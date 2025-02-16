package org.fossify.phone.fragments

//import android.content.Context
//import android.util.AttributeSet
//import org.fossify.commons.helpers.TAB_GROUPS
//import org.fossify.phone.activities.MainActivity
//import org.fossify.phone.activities.SimpleActivity
//import org.fossify.phone.databinding.FragmentGroupsBinding
//import org.fossify.phone.databinding.FragmentLayoutBinding
//import org.fossify.phone.dialogs.CreateNewGroupDialog

//class GroupsFragment(context: Context, attributeSet: AttributeSet) : MyViewPagerFragment<MyViewPagerFragment.FragmentLayout>(context, attributeSet) {
//
//    private lateinit var binding: FragmentGroupsBinding
//
//    override fun onFinishInflate() {
//        super.onFinishInflate()
//        binding = FragmentGroupsBinding.bind(this)
//        innerBinding = FragmentLayout(FragmentLayoutBinding.bind(binding.root))
//    }
//
//    override fun fabClicked() {
//        finishActMode()
//        showNewGroupsDialog()
//    }
//
//    override fun placeholderClicked() {
//        showNewGroupsDialog()
//    }
//
//    private fun showNewGroupsDialog() {
//        CreateNewGroupDialog(activity as SimpleActivity) {
//            (activity as? MainActivity)?.refreshContacts(TAB_GROUPS)
//        }
//    }
//}
