package org.fossify.phone.extensions

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.telecom.PhoneAccount
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import ezvcard.VCardVersion
import org.fossify.commons.activities.BaseSimpleActivity
import org.fossify.commons.dialogs.CallConfirmationDialog
import org.fossify.commons.dialogs.RadioGroupDialog
import org.fossify.commons.extensions.*
import org.fossify.commons.helpers.*
import org.fossify.commons.models.RadioItem
import org.fossify.commons.models.contacts.Contact
import org.fossify.phone.BuildConfig
import org.fossify.phone.R
import org.fossify.phone.activities.DialerActivity
import org.fossify.phone.activities.SimpleActivity
import org.fossify.phone.activities.contacts.EditContactActivity
import org.fossify.phone.activities.contacts.ViewContactActivity
import org.fossify.phone.dialogs.ImportContactsDialog
import org.fossify.phone.dialogs.SelectSIMDialog
import org.fossify.phone.helpers.VcfExporter
import java.io.FileOutputStream

fun SimpleActivity.startCallIntent(recipient: String) {
    if (isDefaultDialer()) {
        getHandleToUse(null, recipient) { handle ->
            launchCallIntent(recipient, handle)
        }
    } else {
        launchCallIntent(recipient, null)
    }
}

fun SimpleActivity.startCallWithConfirmationCheck(recipient: String, name: String) {
    if (config.showCallConfirmation) {
        CallConfirmationDialog(this, name) {
            startCallIntent(recipient)
        }
    } else {
        startCallIntent(recipient)
    }
}

fun SimpleActivity.launchCreateNewContactIntent() {
    Intent().apply {
        action = Intent.ACTION_INSERT
        data = ContactsContract.Contacts.CONTENT_URI
        launchActivityIntent(this)
    }
}

fun BaseSimpleActivity.callContactWithSim(recipient: String, useMainSIM: Boolean) {
    handlePermission(PERMISSION_READ_PHONE_STATE) {
        val wantedSimIndex = if (useMainSIM) 0 else 1
        val handle = getAvailableSIMCardLabels().sortedBy { it.id }.getOrNull(wantedSimIndex)?.handle
        launchCallIntent(recipient, handle)
    }
}

fun BaseSimpleActivity.callContactWithSimWithConfirmationCheck(recipient: String, name: String, useMainSIM: Boolean) {
    if (config.showCallConfirmation) {
        CallConfirmationDialog(this, name) {
            callContactWithSim(recipient, useMainSIM)
        }
    } else {
        callContactWithSim(recipient, useMainSIM)
    }
}

// handle private contacts differently, only Simple Contacts Pro can open them
fun Activity.startContactDetailsIntent(contact: Contact) {
    viewContact(contact)

//    val simpleContacts = "org.fossify.contacts"
//    val simpleContactsDebug = "org.fossify.contacts.debug"
//    if (contact.rawId > 1000000 && contact.contactId > 1000000 && contact.rawId == contact.contactId &&
//        (isPackageInstalled(simpleContacts) || isPackageInstalled(simpleContactsDebug))
//    ) {
//        Intent().apply {
//            action = Intent.ACTION_VIEW
//            putExtra(CONTACT_ID, contact.rawId)
//            putExtra(IS_PRIVATE, true)
//            `package` = if (isPackageInstalled(simpleContacts)) simpleContacts else simpleContactsDebug
//            setDataAndType(ContactsContract.Contacts.CONTENT_LOOKUP_URI, "vnd.android.cursor.dir/person")
//            launchActivityIntent(this)
//        }
//    } else {
//        ensureBackgroundThread {
//            val lookupKey = SimpleContactsHelper(this).getContactLookupKey((contact).rawId.toString())
//            val publicUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey)
//            runOnUiThread {
//                launchViewContactIntent(publicUri)
//            }
//        }
//    }
}

// used at devices with multiple SIM cards
@SuppressLint("MissingPermission")
fun SimpleActivity.getHandleToUse(intent: Intent?, phoneNumber: String, callback: (handle: PhoneAccountHandle?) -> Unit) {
    handlePermission(PERMISSION_READ_PHONE_STATE) {
        if (it) {
            val defaultHandle = telecomManager.getDefaultOutgoingPhoneAccount(PhoneAccount.SCHEME_TEL)
            when {
                intent?.hasExtra(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE) == true -> callback(intent.getParcelableExtra(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE)!!)
                config.getCustomSIM(phoneNumber) != null -> {
                    callback(config.getCustomSIM(phoneNumber))
                }

                defaultHandle != null -> callback(defaultHandle)
                else -> {
                    SelectSIMDialog(this, phoneNumber, onDismiss = {
                        if (this is DialerActivity) {
                            finish()
                        }
                    }) { handle ->
                        callback(handle)
                    }
                }
            }
        }
    }
}


// CONTACTS

fun SimpleActivity.showContactSourcePicker(currentSource: String, callback: (newSource: String) -> Unit) {
    ContactsHelper(this).getSaveableContactSources { sources ->
        val items = ArrayList<RadioItem>()
        var sourceNames = sources.map { it.name }
        var currentSourceIndex = sourceNames.indexOfFirst { it == currentSource }
        sourceNames = sources.map { it.publicName }

        sourceNames.forEachIndexed { index, account ->
            items.add(RadioItem(index, account))
            if (currentSource == SMT_PRIVATE && account == getString(R.string.phone_storage_hidden)) {
                currentSourceIndex = index
            }
        }

        runOnUiThread {
            RadioGroupDialog(this, items, currentSourceIndex) {
                callback(sources[it as Int].name)
            }
        }
    }
}

fun BaseSimpleActivity.shareContacts(contacts: ArrayList<Contact>) {
    val filename = if (contacts.size == 1) {
        "${contacts.first().getNameToDisplay()}.vcf"
    } else {
        DEFAULT_FILE_NAME
    }

    val file = getTempFile(filename)
    if (file == null) {
        toast(org.fossify.commons.R.string.unknown_error_occurred)
        return
    }

    getFileOutputStream(file.toFileDirItem(this), true) {

        // whatsApp does not support vCard version 4.0 yet
        VcfExporter().exportContacts(this, it, contacts, false, version = VCardVersion.V3_0) {
            if (it == VcfExporter.ExportResult.EXPORT_OK) {
                sharePathIntent(file.absolutePath, BuildConfig.APPLICATION_ID)
            } else {
                showErrorToast("$it")
            }
        }
    }
}

fun SimpleActivity.handleGenericContactClick(contact: Contact) {
    when (config.onContactClick) {
        ON_CLICK_CALL_CONTACT -> callContact(contact)
        ON_CLICK_VIEW_CONTACT -> viewContact(contact)
        ON_CLICK_EDIT_CONTACT -> editContact(contact)
    }
}

fun SimpleActivity.callContact(contact: Contact) {
    hideKeyboard()
    if (contact.phoneNumbers.isNotEmpty()) {
        tryInitiateCall(contact) { startCallIntent(it) }
    } else {
        toast(org.fossify.commons.R.string.no_phone_number_found)
    }
}

fun Activity.viewContact(contact: Contact) {
    hideKeyboard()
    Intent(applicationContext, ViewContactActivity::class.java).apply {
        putExtra(CONTACT_ID, contact.id)
        putExtra(IS_PRIVATE, contact.isPrivate())
        startActivity(this)
    }
}

fun Activity.editContact(contact: Contact) {
    hideKeyboard()
    Intent(applicationContext, EditContactActivity::class.java).apply {
        putExtra(CONTACT_ID, contact.id)
        putExtra(IS_PRIVATE, contact.isPrivate())
        startActivity(this)
    }
}

fun SimpleActivity.tryImportContactsFromFile(uri: Uri, callback: (Boolean) -> Unit) {
    when (uri.scheme) {
        "file" -> showImportContactsDialog(uri.path!!, callback)
        "content" -> {
            val tempFile = getTempFile()
            if (tempFile == null) {
                toast(org.fossify.commons.R.string.unknown_error_occurred)
                return
            }

            try {
                val inputStream = contentResolver.openInputStream(uri)
                val out = FileOutputStream(tempFile)
                inputStream!!.copyTo(out)
                showImportContactsDialog(tempFile.absolutePath, callback)
            } catch (e: Exception) {
                showErrorToast(e)
            }
        }

        else -> toast(org.fossify.commons.R.string.invalid_file_format)
    }
}

fun SimpleActivity.showImportContactsDialog(path: String, callback: (Boolean) -> Unit) {
    ImportContactsDialog(this, path, callback)
}


