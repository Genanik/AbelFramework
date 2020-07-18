package io.genanik.abel

import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.plugins.PluginBase
import net.mamoe.mirai.event.*
import net.mamoe.mirai.event.events.NewFriendRequestEvent
import net.mamoe.mirai.message.data.*
import java.io.File

object AbelFramework : PluginBase() {

    val abelBotVersion = "3.0"
    val bot = AbelPluginsManager(logger)

    override fun onLoad() {
        super.onLoad()

        // 注册Abel管理员指令
        logger.info("开始注册Abel管理员指令")
        bot.regAdminCommand("/adminHelp") {
            val result = MessageChainBuilder()
            result.add("启用{功能}\n")
            result.add("禁用{功能}\n")
            result.add("切换{功能}\n")
            result.add(
                "AbelUserPluginController: \n" +
                "${bot.getAllCommands()}\n" + // 指令
                "${bot.getAllFunctions()}\n"  // 功能
            )
            result.add(
                "AbelAdminPluginController: \n" +
                        "${bot.adminGetAllCommands()}\n" + // 指令
                        "${bot.adminGetAllFunctions()}\n"  // 功能
            )
            result.add("AbelVersion: $abelBotVersion\n")
            result.add("JavaVersion: ${System.getProperty("java.version")}\n")
            result.add("MiraiCoreVersion: ${File(Bot.javaClass.protectionDomain.codeSource.location.file).name
                    .replace(".jar", "")}\n"
            )
            result.add("MiraiConsleVersion: ${MiraiConsole.version} - ${MiraiConsole.build}")
            return@regAdminCommand result.asMessageChain()
        }
        // 注册Abel指令
        logger.info("开始注册Abel指令")
        bot.regCommand("/help", "展示帮助信息") {
            val result = MessageChainBuilder()
            result.add("你好你好你好你好\n这里是Abel指路中心\n")
            for (i in bot.getAllCommands()) {
                result.add("* $i  ${bot.getCommandDescription()[i]}\n")
            }
            result.add("\n咱介绍完指令，该介绍功能了\n\n")
            for (i in bot.getAllFunctions()) {
                result.add("* $i  ${bot.getFunctionDescription()[i]}\n")
            }
            result.add(
                "\n其他功能：\n" +
                "* \"功能名称+打开了嘛\" 获取功能运行状态\n" +
                "* /adminHelp 获取管理员帮助信息"
            )
            return@regCommand result.asMessageChain()
        }
    }

    override fun onEnable() {
        super.onEnable()
        logger.info("Abel Framework loaded!")

        /**
         * 订阅Abel内置user指令
         */
        subscribeGroupMessages {
            // 普通用户
            for (i in bot.getAllFunctions()) {
                // 操作
                case("关闭$i") {
                    if (!this@AbelFramework.bot.adminGetStatus(i, this.group.id)) {
                        if (this@AbelFramework.bot.getStatus(i, this.group.id)) {
                            this@AbelFramework.bot.disableFunc(i, this.group.id)
                            reply("不出意外的话。。咱关掉${i}了")
                        } else {
                            reply(
                                "这个功能已经被关掉了呢_(:з」∠)_不用再关一次了\n" +
                                        "推荐使用\"功能名称+打开了嘛\"获取功能状态"
                            )
                        }
                    }
                }
                case("开启$i") {
                    if (!this@AbelFramework.bot.adminGetStatus(i, this.group.id)) {
                        if (!this@AbelFramework.bot.getStatus(i, this.group.id)) {
                            this@AbelFramework.bot.enableFunc(i, this.group.id)
                            reply("不出意外的话。。咱打开${i}了")
                        } else {
                            reply(
                                "(｡･ω･)ﾉﾞ${i}\n这个已经打开了哦，不用再开一次啦\n" +
                                        "推荐使用\"功能名称+打开了嘛\"获取功能状态"
                            )
                        }
                    }
                }

                // 查询
                case("${i}打开了嘛") {
                    var status = this@AbelFramework.bot.getStatus(i, this.group.id)
                    if (i == "翻译") {
                        status = !status
                    }
                    if (status) {
                        reply("开啦(′▽`〃)")
                    } else {
                        reply("没有ヽ(･ω･｡)ﾉ ")
                    }
                }

                // Abel外置插件实现
                case(i){
                    reply(this@AbelFramework.bot.adminTransferCommand(i)(this.group.id))
                }
                case(i) {
                    reply(this@AbelFramework.bot.transferCommand(i)(this.group.id))
                }
            }
        }

        /**
         * 订阅Abel内置admin指令
         */
        subscribeGroupMessages(priority = EventPriority.HIGH) {
            // admin指令实现
            for (i in bot.adminGetAllCommands()) {
                case(i) {
                    reply(this@AbelFramework.bot.adminTransferCommand(i)(this.group.id))
                }
            }
            for (i in bot.getAllCommands()) {
                case(i) {
                    reply(this@AbelFramework.bot.transferCommand(i)(this.group.id))
                }
            }
        }

        // 禁用/启用实现
        subscribeGroupMessages(priority = EventPriority.HIGH){
            for (i in bot.adminGetAllFunctions()) {
                // 操作
                case("禁用$i") {
                    if (this@AbelFramework.bot.isAdmin(this.sender.id)) {

                        this@AbelFramework.bot.adminDisableFunc(i, this.group.id)
                        this@AbelFramework.bot.disableFunc(i, this.group.id)
                        reply("群: ${this.group.id}\n已禁用功能: $i")
                    }
                }
                case("启用$i") {
                    if (this@AbelFramework.bot.isAdmin(this.sender.id)) {

                        this@AbelFramework.bot.adminEnableFunc(i, this.group.id)
                        this@AbelFramework.bot.enableFunc(i, this.group.id)
                        reply("群: ${this.group.id}\n已启用功能: $i")
                    }
                }
            }
        }

        // 临时消息
        subscribeTempMessages {
            always {
                reply("emm抱歉。。暂不支持临时会话，但是可以通过邀请至群使用（加好友自动通过验证），群内/help查看帮助")
            }
        }

        // 好友消息
        subscribeFriendMessages {
            always {
                reply("emm抱歉。。暂不支持私聊，但是可以通过邀请至群使用（加好友自动通过验证），群内/help查看帮助")
            }
        }

        // 自动同意新好友
        subscribeAlways<NewFriendRequestEvent> {
            accept()
        }

    }
}