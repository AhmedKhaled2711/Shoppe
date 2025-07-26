package com.lee.shoppe.data.model

import android.content.Context
import com.lee.shoppe.data.network.caching.SharedPreferenceManager

class CustomerData private constructor(_context: Context) {
    private lateinit var manager: SharedPreferenceManager
    
    // Track if we're in guest mode with preserved cart/favorites
    private var _isGuestWithPreservedData = false
    var isGuestWithPreservedData: Boolean
        get() = _isGuestWithPreservedData
        set(value) {
            _isGuestWithPreservedData = value
            // Safe to access manager here as it's initialized before this can be set
            manager.save(SharedPreferenceManager.Key.IS_GUEST_WITH_PRESERVED_DATA, value.toString())
        }

    var isLogged: Boolean = false
        get() = field
        set(value) {
            field = value
            manager.save(SharedPreferenceManager.Key.IS_LOGGED_IN, value.toString())
        }
    var id: Long = 0
        get() = field
        set(value) {
            field = value
            manager.save(SharedPreferenceManager.Key.ID, value.toString())
        }
    var name: String = ""
        get() = field
        set(value) {
            field = value
            manager.save(SharedPreferenceManager.Key.NAME, value)
        }
    var email: String = ""
        get() = field
        set(value) {
            field = value
            manager.save(SharedPreferenceManager.Key.EMAIL, value)
        }
    var phone: String = ""
        get() = field
        set(value) {
            field = value
            manager.save(SharedPreferenceManager.Key.PHONE, value)
        }
    var currency: String = ""
        get() = field
        set(value) {
            field = value
            manager.save(SharedPreferenceManager.Key.CURRENCY, value)
        }
    var favListId: Long = 0
        get() = field
        set(value) {
            field = value
            manager.save(SharedPreferenceManager.Key.FavListID, value.toString())
        }
    var cartListId: Long = 0
        get() = field
        set(value) {
            field = value
            manager.save(SharedPreferenceManager.Key.CartListID, value.toString())
        }
    var language: String = ""
        get() = field
        set(value) {
            field = value
            manager.save(SharedPreferenceManager.Key.Language, value)
        }
    var languageCode: String = ""
        get() = field
        set(value) {
            field = value
            manager.save(SharedPreferenceManager.Key.LanguageCode, value)
        }


    init {
        manager = SharedPreferenceManager(_context)
        
        // Initialize properties after manager is set
        _isGuestWithPreservedData = manager.retrieve(SharedPreferenceManager.Key.IS_GUEST_WITH_PRESERVED_DATA, "false").toBoolean()
        isLogged = manager.retrieve(SharedPreferenceManager.Key.IS_LOGGED_IN, "false").toBoolean()
        
        if (_isGuestWithPreservedData) {
            // Load guest data
            cartListId = manager.retrieve(SharedPreferenceManager.Key.GUEST_CART_ID, "0").toLong()
            favListId = manager.retrieve(SharedPreferenceManager.Key.GUEST_FAV_ID, "0").toLong()
            currency = manager.retrieve(SharedPreferenceManager.Key.GUEST_CURRENCY, "EGY")
            language = manager.retrieve(SharedPreferenceManager.Key.GUEST_LANGUAGE, "")
            languageCode = manager.retrieve(SharedPreferenceManager.Key.GUEST_LANGUAGE_CODE, "en")
            
            // Clear regular user data
            id = 0
            name = ""
            email = ""
            phone = ""
        } else {
            // Load regular user data
            id = manager.retrieve(SharedPreferenceManager.Key.ID, "0").toLong()
            name = manager.retrieve(SharedPreferenceManager.Key.NAME, "")
            email = manager.retrieve(SharedPreferenceManager.Key.EMAIL, "")
            phone = manager.retrieve(SharedPreferenceManager.Key.PHONE, "")
            currency = manager.retrieve(SharedPreferenceManager.Key.CURRENCY, "EGY")
            favListId = manager.retrieve(SharedPreferenceManager.Key.FavListID, "0").toLong()
            cartListId = manager.retrieve(SharedPreferenceManager.Key.CartListID, "0").toLong()
            language = manager.retrieve(SharedPreferenceManager.Key.Language, "")
            languageCode = manager.retrieve(SharedPreferenceManager.Key.LanguageCode, "en")
            
            // Clear guest data if any exists
            if (!isLogged) {
                manager.save(SharedPreferenceManager.Key.GUEST_CART_ID, "0")
                manager.save(SharedPreferenceManager.Key.GUEST_FAV_ID, "0")
                manager.save(SharedPreferenceManager.Key.GUEST_CURRENCY, "")
                manager.save(SharedPreferenceManager.Key.GUEST_LANGUAGE, "")
                manager.save(SharedPreferenceManager.Key.GUEST_LANGUAGE_CODE, "")
            }
        }
    }

    companion object {
        @Volatile
        private var instance: CustomerData? = null
        fun getInstance(context: Context): CustomerData {
            if (instance == null)
                instance = CustomerData(context)
            return instance as CustomerData
        }
    }

    fun logOut() {
        // Only preserve cart/favorites if we have valid IDs
        val shouldPreserveData = cartListId > 0 || favListId > 0
        
        if (shouldPreserveData) {
            // Set guest mode before clearing session data
            isGuestWithPreservedData = true
            
            // Save the current state as guest data
            manager.save(SharedPreferenceManager.Key.GUEST_CART_ID, cartListId.toString())
            manager.save(SharedPreferenceManager.Key.GUEST_FAV_ID, favListId.toString())
            manager.save(SharedPreferenceManager.Key.GUEST_CURRENCY, currency)
            manager.save(SharedPreferenceManager.Key.GUEST_LANGUAGE, language)
            manager.save(SharedPreferenceManager.Key.GUEST_LANGUAGE_CODE, languageCode)
            
            // Save current currency and language as fallbacks
            manager.save(SharedPreferenceManager.Key.CURRENCY, currency)
            manager.save(SharedPreferenceManager.Key.Language, language)
            manager.save(SharedPreferenceManager.Key.LanguageCode, languageCode)
        } else {
            isGuestWithPreservedData = false
            // Clear any existing guest data
            manager.save(SharedPreferenceManager.Key.GUEST_CART_ID, "0")
            manager.save(SharedPreferenceManager.Key.GUEST_FAV_ID, "0")
            manager.save(SharedPreferenceManager.Key.GUEST_CURRENCY, "")
            manager.save(SharedPreferenceManager.Key.GUEST_LANGUAGE, "")
            manager.save(SharedPreferenceManager.Key.GUEST_LANGUAGE_CODE, "")
        }
        
        // Clear user session data
        manager.save(SharedPreferenceManager.Key.ID, "0")
        manager.save(SharedPreferenceManager.Key.NAME, "")
        manager.save(SharedPreferenceManager.Key.EMAIL, "")
        manager.save(SharedPreferenceManager.Key.PHONE, "")
        manager.save(SharedPreferenceManager.Key.IS_LOGGED_IN, "false")
        
        // Reset session fields
        id = 0
        name = ""
        email = ""
        phone = ""
        isLogged = false
        
        if (isGuestWithPreservedData) {
            // Load guest data from preferences
            cartListId = manager.retrieve(SharedPreferenceManager.Key.GUEST_CART_ID, "0").toLong()
            favListId = manager.retrieve(SharedPreferenceManager.Key.GUEST_FAV_ID, "0").toLong()
            currency = manager.retrieve(SharedPreferenceManager.Key.GUEST_CURRENCY, "EGY")
            language = manager.retrieve(SharedPreferenceManager.Key.GUEST_LANGUAGE, "")
            languageCode = manager.retrieve(SharedPreferenceManager.Key.GUEST_LANGUAGE_CODE, "en")
        } else {
            // Clear cart/favorite data
            cartListId = 0
            favListId = 0
            
            // Load default currency and language
            currency = manager.retrieve(SharedPreferenceManager.Key.CURRENCY, "EGY")
            language = manager.retrieve(SharedPreferenceManager.Key.Language, "")
            languageCode = manager.retrieve(SharedPreferenceManager.Key.LanguageCode, "en")
        }
    }
}