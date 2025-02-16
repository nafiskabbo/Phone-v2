package org.fossify.phone.fragments

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import org.fossify.commons.adapters.MyRecyclerViewAdapter
import org.fossify.commons.extensions.*
import org.fossify.commons.helpers.*
import org.fossify.commons.views.MyFloatingActionButton
import org.fossify.commons.views.MyRecyclerView
import org.fossify.phone.activities.MainActivity
import org.fossify.phone.activities.SimpleActivity
import org.fossify.phone.activities.contacts.InsertOrEditContactActivity
import org.fossify.phone.adapters.ContactsAdapter
import org.fossify.phone.adapters.RecentCallsAdapter
import org.fossify.phone.databinding.FragmentLettersLayoutBinding
import org.fossify.phone.databinding.FragmentRecentsBinding
import org.fossify.phone.extensions.config
import org.fossify.phone.helpers.Config

abstract class MyViewPagerFragment<BINDING : MyViewPagerFragment.InnerBinding>(context: Context, attributeSet: AttributeSet) :
    RelativeLayout(context, attributeSet) {
    protected var activity: SimpleActivity? = null
    protected lateinit var innerBinding: BINDING
    private lateinit var config: Config

//    protected var allContacts = ArrayList<Contact>()

    private var lastHashCode = 0

    var skipHashComparing = false
    var forceListRedraw = false

    fun setupFragment(activity: SimpleActivity) {
        config = activity.config
        if (this.activity == null) {
            this.activity = activity
            innerBinding.fragmentFab?.beGoneIf(activity is InsertOrEditContactActivity)
            innerBinding.fragmentFab?.setOnClickListener {
                fabClicked()
            }

            setupFragment()
            setupColors(activity.getProperTextColor(), activity.getProperPrimaryColor(), activity.getProperPrimaryColor())
        }
    }

    fun startNameWithSurnameChanged(startNameWithSurname: Boolean) {
        if (this !is RecentsFragment) {
            (innerBinding.fragmentList?.adapter as? ContactsAdapter)?.apply {
                config.sorting = if (startNameWithSurname) SORT_BY_SURNAME else SORT_BY_FIRST_NAME
                (this@MyViewPagerFragment.activity!! as MainActivity).refreshFragments()
            }
        }
    }

    fun finishActMode() {
        (innerBinding.fragmentList?.adapter as? MyRecyclerViewAdapter)?.finishActMode()
        (innerBinding.recentsList?.adapter as? MyRecyclerViewAdapter)?.finishActMode()
    }

    fun fontSizeChanged() {
        if (this is RecentsFragment) {
            (innerBinding.recentsList.adapter as? RecentCallsAdapter)?.apply {
                fontSize = activity.getTextSize()
                notifyDataSetChanged()
            }
        } else {
            (innerBinding.fragmentList?.adapter as? ContactsAdapter)?.apply {
                fontSize = activity.getTextSize()
                notifyDataSetChanged()
            }
        }
    }

//    fun refreshContacts(contacts: ArrayList<Contact>, placeholderText: String? = null) {
//        if ((config.showTabs and TAB_CONTACTS == 0 && this is ContactsFragment && activity !is InsertOrEditContactActivity) ||
//            (config.showTabs and TAB_FAVORITES == 0 && this is FavoritesFragment)
////            ||
////            (config.showTabs and TAB_GROUPS == 0 && this is GroupsFragment)
//        ) {
//            return
//        }
//
//        if (config.lastUsedContactSource.isEmpty()) {
//            val grouped = contacts.groupBy { it.source }.maxWithOrNull(compareBy { it.value.size })
//            config.lastUsedContactSource = grouped?.key ?: ""
//        }
//
//        allContacts = contacts
//        val filtered = when (this) {
////            is GroupsFragment -> contacts
//            is FavoritesFragment -> {
//                val favouriteContacts = contacts.filter { it.starred == 1 }
//
//                if (activity!!.config.isCustomOrderSelected) {
//                    sortFavourites(favouriteContacts)
//                } else {
//                    favouriteContacts
//                }
//            }
//
//            else -> {
//                val contactSources = activity!!.getVisibleContactSources()
//                contacts.filter { contactSources.contains(it.source) }
//            }
//        }
//
//        var currentHash = 0
//        filtered.forEach {
//            currentHash += it.getHashWithoutPrivatePhoto()
//        }
//
//        if (currentHash != lastHashCode || skipHashComparing || filtered.isEmpty()) {
//            skipHashComparing = false
//            lastHashCode = currentHash
//            activity?.runOnUiThread {
//                setupContacts(filtered)
//
//                if (placeholderText != null) {
//                    innerBinding.fragmentPlaceholder.text = placeholderText
//                    innerBinding.fragmentPlaceholder.tag = AVOID_CHANGING_TEXT_TAG
//                    innerBinding.fragmentPlaceholder2.beGone()
//                    innerBinding.fragmentPlaceholder2.tag = AVOID_CHANGING_VISIBILITY_TAG
//                }
//            }
//        }
//    }


    abstract fun setupFragment()

    abstract fun setupColors(textColor: Int, primaryColor: Int, properPrimaryColor: Int)

    abstract fun onSearchClosed()

    abstract fun onSearchQueryChanged(text: String)

    abstract fun fabClicked()

    interface InnerBinding {
        val fragmentList: MyRecyclerView?
        val recentsList: MyRecyclerView?
        val fragmentFab: MyFloatingActionButton?
    }

    class LettersInnerBinding(val binding: FragmentLettersLayoutBinding) : InnerBinding {
        override val fragmentList: MyRecyclerView = binding.fragmentList
        override val recentsList = null
        override val fragmentFab: MyFloatingActionButton = binding.fragmentFab
    }

    class RecentsInnerBinding(val binding: FragmentRecentsBinding) : InnerBinding {
        override val fragmentList = null
        override val recentsList = binding.recentsList
        override val fragmentFab: MyFloatingActionButton? = null
    }
}
