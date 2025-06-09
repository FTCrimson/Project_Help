package data.commandchat_api

import java.util.Date

data class Chat(
    val id: String = "",
    val name: String = "New Chat",
    val creatorId: String = "",
    val members: List<String> = emptyList(),
    val createdAt: Date? = null,
    val lastMessageTimestamp: Date? = null,

    val currentStage: Int = 1,
    val stage1Approvals: Map<String, Boolean> = emptyMap(),
    val stage2Approvals: Map<String, Boolean> = emptyMap()
)
