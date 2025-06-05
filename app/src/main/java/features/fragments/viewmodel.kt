package features.fragments

import androidx.lifecycle.ViewModel
import com.example.project_helper.data.auth.RoleSelection

class ProfileViewModel : ViewModel() {
    private var roleSelection: RoleSelection? = null

    fun saveRoleSelection(selection: RoleSelection) {
        roleSelection = selection
    }

    fun getRoleSelection(): RoleSelection? = roleSelection
}