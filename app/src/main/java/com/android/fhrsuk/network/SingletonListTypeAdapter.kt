package com.android.fhrsuk.network

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.io.IOException
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.WildcardType
import java.util.*
import kotlin.collections.ArrayList

//https://stackoverflow.com/a/47420751
//necessary helper class because if json api response contains 1 items it is a single object
//if the response is more than 1 item then it is a list of objects
//this adapter converts any single responses to a list of 1 to match the model classes
class SingletonListTypeAdapter<T>(private val delegate: TypeAdapter<T>) :
    TypeAdapter<List<T>>() {

    @Throws(IOException::class)
    override fun write(out: JsonWriter, value: List<T>) {
        out.beginArray()
        var i = 0
        val size = value.size
        while (i < size) {
            delegate.write(out, value[i])
            i++
        }
        out.endArray()
    }

    @Throws(IOException::class)
    override fun read(`in`: JsonReader): List<T> {
        if (`in`.peek() != JsonToken.BEGIN_ARRAY) {
            return Collections.singletonList(delegate.read(`in`))
        }
        `in`.beginArray()
        val expanding = ArrayList<T>()
        while (`in`.hasNext()) {
            expanding.add(delegate.read(`in`))
        }
        `in`.endArray()
        return Collections.unmodifiableList(expanding)
    }

    companion object {
        val FACTORY: TypeAdapterFactory = object : TypeAdapterFactory {
            override fun <T> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
                if (type.rawType != List::class.java) {//List<*>::class.java) {
                    return null
                }
                val collectionElementType = TypeToken.get(
                    getCollectionElementType(
                        type.type as ParameterizedType
                    )
                )
                val delegate = gson.getDelegateAdapter(//<*>(
                    this,
                    collectionElementType
                ) as TypeAdapter<List<Any>>
                return SingletonListTypeAdapter(delegate) as TypeAdapter<T>
            }
        }

        fun getCollectionElementType(type: ParameterizedType): Type {
            val types = type.actualTypeArguments
            val paramType = types[0]
            return if (paramType is WildcardType) {
                paramType.upperBounds[0]
            } else paramType
        }
    }
}