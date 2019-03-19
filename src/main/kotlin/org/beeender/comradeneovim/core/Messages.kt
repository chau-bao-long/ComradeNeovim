package org.beeender.comradeneovim.core

import org.beeender.neovim.annotation.MessageConverterFun
import org.beeender.neovim.rpc.Notification

class ComradeBufEnterParams(val id: Int, val path: String) {
    companion object {
        fun fromNotification(notification: Notification) : ComradeBufEnterParams {
            val map = notification.args.first() as Map<*, *>
            val id = map["id"] as Int
            val path =  map["path"] as String
            return ComradeBufEnterParams(id, path)
        }

        fun toNotification(params: ComradeBufEnterParams) : Notification {
            val map = mutableMapOf<Any, Any>()
            map["id"] = params.id
            map["path"] = params.path
            return Notification(MSG_COMRADE_BUF_ENTER, listOf(map) )
        }
    }
}

class ComradeBufWriteParams(val id: Int) {
    companion object {
        @MessageConverterFun
        fun fromNotification(notification: Notification) : ComradeBufWriteParams {
            val bufId = (notification.args[0] as Map<*, *>)["id"] as Int
            return ComradeBufWriteParams(bufId)
        }
    }
}
