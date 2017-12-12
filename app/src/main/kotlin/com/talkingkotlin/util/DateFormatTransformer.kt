package com.talkingkotlin.util

import org.simpleframework.xml.transform.Transform
import java.text.DateFormat
import java.util.*

/**
 * Date Transformer for XML Parsing
 * @author Alexander Gherschon
 */

class DateFormatTransformer(private val dateFormat: DateFormat) : Transform<Date> {

    @Throws(Exception::class)
    override fun read(value: String): Date = dateFormat.parse(value)

    @Throws(Exception::class)
    override fun write(value: Date): String = dateFormat.format(value)
}