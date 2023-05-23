package com.woleapp.netpos.contactless.util

import android.text.Editable
import android.text.Html
import org.xml.sax.XMLReader

class CustomTagHandler : Html.TagHandler {
    override fun handleTag(opening: Boolean, tag: String?, output: Editable?, xmlReader: XMLReader?) {
        // Ignore all tags and only include plain text content
    }
}
