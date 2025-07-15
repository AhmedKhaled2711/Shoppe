package com.lee.shoppe.data.model

import android.content.Context
import com.lee.shoppe.data.network.caching.SharedPreferenceManager

class CustomerData private constructor(_context: Context) {
    private val manager: SharedPreferenceManager

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
        id = manager.retrieve(SharedPreferenceManager.Key.ID, "0").toLong()
        name = manager.retrieve(SharedPreferenceManager.Key.NAME, "")
        email = manager.retrieve(SharedPreferenceManager.Key.EMAIL, "")
        phone = manager.retrieve(SharedPreferenceManager.Key.PHONE, "")
        currency = manager.retrieve(SharedPreferenceManager.Key.CURRENCY, "EGY")
        favListId = manager.retrieve(SharedPreferenceManager.Key.FavListID, "0").toLong()
        cartListId = manager.retrieve(SharedPreferenceManager.Key.CartListID, "0").toLong()
        language = manager.retrieve(SharedPreferenceManager.Key.Language, "")
        languageCode = manager.retrieve(SharedPreferenceManager.Key.LanguageCode, "en")
        isLogged = manager.retrieve(SharedPreferenceManager.Key.IS_LOGGED_IN, "false").toBoolean()

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
        instance = null
        manager.clear()
    }
}