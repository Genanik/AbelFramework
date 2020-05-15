package io.genanik.abel

import net.mamoe.mirai.console.plugins.PluginBase
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.utils.info

object Framework : PluginBase() {

    lateinit var apm: AbelPluginsManager

    override fun onLoad() {
        super.onLoad()
    }

    override fun onEnable() {
        super.onEnable()

        logger.info("Abel插件管理器loaded")

        // 绑定command
        subscribeGroupMessages {

            // 绑定管理员command
            for (i in apm.adminGetAllCommands()){
                case(i) {
                    reply( apm.adminTransferCommand(i)(this.group.id))
                }
            }

            // 绑定用户指令
            for (i in apm.getAllCommands()){
                case(i) {
                    reply( apm.transferCommand(i)(this.group.id))
                }
            }
        }



    }

}