import dev.inmo.tgbotapi.extensions.api.send.sendMessage
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.extensions.utils.usernameChatOrNull

suspend fun BehaviourContext.addFriend() {
    onCommand("add_friend") {
        val user = createIfNotExist(it.chat)
        var username = waitText(it.chat, getSendText(it.chat, "add_friend", user.preferredName))
        username = (if (username.first() != '@') "@" else "") + username
        val entry =
            users.entries.find { entry -> entry.value.username == username && entry.value.username != user.username }
        entry?.let { friend ->
            user.friends.add(username)
            sendMessageBundled(it.chat, "friend_added", username, friend.value.preferredName)
            return@onCommand
        }
        sendMessageBundled(it.chat, "no_user", username)
    }
}

suspend fun BehaviourContext.showFriends() {
    onCommand("show_friends") {
        val user = createIfNotExist(it.chat)
        val friends = users.filter { friend -> friend.value.username in user.friends }
            .map { friend -> "${friend.value.username} - ${friend.value.preferredName}" }
            .joinToString("\n")
        sendMessage(it.chat, friends)
    }
}

suspend fun BehaviourContext.updateTag() {
    onCommand("update_tag") {
        val user = createIfNotExist(it.chat)
        val username = it.chat.usernameChatOrNull()?.username?.username
        username?.let { usernameFinal ->
            user.username = usernameFinal
            sendMessageBundled(it.chat, "username_changed_success", username)
            return@onCommand
        }
        sendMessageBundled(it.chat, "username_changed_fail")
    }
}
