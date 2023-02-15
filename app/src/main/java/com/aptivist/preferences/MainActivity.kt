package com.aptivist.preferences

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.aptivist.preferences.domain.models.AppPreferences
import com.aptivist.preferences.domain.models.AppPreferencesSerializer
import com.aptivist.preferences.ui.theme.PreferencesTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    //Shared Preferences
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    //DataStore Preferences
    private lateinit var mDataStore : DataStore<Preferences>
    private val Context.createDataStore by preferencesDataStore("myPreferences")

    //DataStore
    private val Context.dataStoreJson by dataStore("appPreferences.json", AppPreferencesSerializer)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PreferencesTheme {

                val coroutineScope = rememberCoroutineScope()

                val text = remember {
                    mutableStateOf("")
                }

                val preferences = remember {
                    mutableStateOf(AppPreferences())
                }

                LaunchedEffect(true){
                    dataStoreJson.data.collectLatest {
                        preferences.value = it
                    }
                }

                Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    TextField(value = text.value, onValueChange = {
                        text.value = it
                    })
                    Row() {
                        Button(onClick = {
                            //Shared preferences
                            sharedPreferencesSave(TEXT_KEY, text.value)

                            //DataStore Preferences
                            //coroutineScope.launch(Dispatchers.IO) {
                            //    dataStorePreferencesSave(TEXT_KEY, text.value)
                            //}

                            //DataStore
                            //coroutineScope.launch(Dispatchers.IO) {
                            //   dataStoreSave(text.value)
                            //}

                        }) {
                            Text(text = "Save")
                        }
                        Spacer(modifier = Modifier.size(8.dp))
                        Button(onClick = {
                            //Shared preferences
                             val getText = sharedPreferencesGet(TEXT_KEY)
                             text.value = getText.orEmpty()

                            //DataStore Preferences
                            //coroutineScope.launch(Dispatchers.IO) {
                            //    val getText = dataStorePreferencesGet(TEXT_KEY)
                            //    text.value = getText.orEmpty()
                            //}

                            //DataStore
                            //text.value = preferences.value.name.orEmpty()

                        }) {
                            Text(text = "Load")
                        }
                        Spacer(modifier = Modifier.size(8.dp))
                        Button(onClick = {
                            //Shared preferences
                             sharedPreferencesClear()

                            //DataStore Preferences
                            //coroutineScope.launch(Dispatchers.IO) {
                            //    dataStorePreferencesClear()
                            //}

                            //DataStore
                            //coroutineScope.launch(Dispatchers.IO) {
                             //   dataStoreClear()
                            //}

                        }) {
                            Text(text = "Clear")
                        }
                    }
                }
            }
        }

        //sharedPreferences = getSharedPreferences("myPreferences", Context.MODE_PRIVATE)
        //editor = sharedPreferences.edit()

        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        sharedPreferences = EncryptedSharedPreferences.create(
            "myPreferences",
            masterKeyAlias,
            this@MainActivity,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        editor = sharedPreferences.edit()

        mDataStore = createDataStore
    }


    private fun sharedPreferencesClear() {
        editor.apply {
            clear()
            apply()
        }
    }

    private fun sharedPreferencesSave(key: String, value: String) {
        editor.apply {
            putString(key, value)
            apply()
        }
    }

    private fun sharedPreferencesGet(key: String) : String? {
        return sharedPreferences.getString(key, "Not Found")
    }

    private suspend fun dataStorePreferencesClear() {
        mDataStore.edit {
            it.clear()
        }
    }

    private suspend fun dataStorePreferencesSave(key: String, value: String) {
        val dataStoreKey = stringPreferencesKey(key)
        mDataStore.edit { preferences ->
            preferences[dataStoreKey] = value
        }
    }

    private suspend fun dataStorePreferencesGet(key: String) : String? {
        val dataStoreKey = stringPreferencesKey(key)
        val preferences = mDataStore.data.first()
        return preferences[dataStoreKey]
    }

    private suspend fun dataStoreSave(value: String){
        dataStoreJson.updateData {
            it.copy(name = value)
        }
    }

    private suspend fun dataStoreClear() {
        dataStoreJson.updateData {
            AppPreferences()
        }
    }

}

const val TEXT_KEY = "text"