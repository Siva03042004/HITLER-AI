package com.tcprivatelimited.hitlerai.data.model

import androidx.annotation.Keep
import androidx.compose.ui.graphics.Color
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@Keep
@IgnoreExtraProperties
data class Conversation(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val activeAgent: String? = null,
    val lastMessagePreview: String = "",
    val messageCount: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class ChatMessage(
    @DocumentId val id: String = "",
    val userId: String = "",
    val role: String = "user",
    val senderLabel: String = "YOU",
    val content: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class CouncilSession(
    @DocumentId val id: String = "",
    val userId: String = "",
    val prompt: String = "",
    val edithResponse: String = "",
    val visionResponse: String = "",
    val ultronResponse: String = "",
    val fridayResponse: String = "",
    val jarvisResponse: String = "",
    val synthesis: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class FeedPost(
    @DocumentId val id: String = "",
    val userId: String = "",
    val authorName: String = "",
    val text: String = "",
    val likes: Long = 0L,
    val likedByMe: Boolean = false,
    val comments: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class LibraryItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val category: String = "chat",
    val content: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

data class AiSpecialist(
    val name: String,
    val role: String,
    val description: String,
    val letter: String,
    val accentColor: Color,
    val councilStyle: String,
    val promptPrefix: String
)

data class QuickPromptItem(
    val title: String,
    val subtitle: String,
    val promptPrefix: String
)

data class WorkTemplateItem(
    val title: String,
    val subtitle: String,
    val promptPrefix: String
)

data class DiscoverTopicItem(
    val title: String,
    val subtitle: String,
    val promptPrefix: String
)

enum class WorkspaceDestination(val label: String, val routeTag: String) {
    CHAT("AI Workspace", "chat"),
    COUNCIL("AI Council", "council"),
    AGENTS("My AI Team", "agents"),
    FEED("Social Feed", "feed"),
    MESSAGES("Messages", "messages"),
    DISCOVER("Discover", "discover"),
    WORK("Work Studio", "work"),
    LIBRARY("Library", "library"),
    CUSTOMIZE("Customize", "customize")
}

object WorkspaceCatalog {
    val specialists: List<AiSpecialist> = listOf(
        AiSpecialist(
            name = "Ultron",
            role = "Strategy & optimization",
            description = "Challenges assumptions, finds bottlenecks and improves plans.",
            letter = "U",
            accentColor = Color(0xFFFF827C),
            councilStyle = "Direct and rigorous. Stress-test assumptions, identify trade-offs and improve efficiency. Be constructive, never hostile.",
            promptPrefix = "Act as Ultron, my strategy and optimization specialist. Challenge assumptions constructively and help me improve this task: "
        ),
        AiSpecialist(
            name = "Vision",
            role = "Analysis & foresight",
            description = "Explores scenarios, trade-offs, systems and long-term outcomes.",
            letter = "V",
            accentColor = Color(0xFFE8BD61),
            councilStyle = "Thoughtful and measured. Explore future possibilities, risks and second-order effects.",
            promptPrefix = "Act as Vision, my analysis and foresight specialist. Explore scenarios, trade-offs and long-term implications for: "
        ),
        AiSpecialist(
            name = "Jarvis",
            role = "Personal assistant & quality",
            description = "Organizes tasks, checks quality and helps you make informed decisions.",
            letter = "J",
            accentColor = Color(0xFF61D7B8),
            councilStyle = "Professional and calm. Check accuracy, privacy, permissions, security and quality.",
            promptPrefix = "Act as Jarvis, my personal assistant. Organize this request into a clear, high-quality response: "
        ),
        AiSpecialist(
            name = "Friday",
            role = "Execution & delivery",
            description = "Turns decisions into practical steps, drafts and deliverables.",
            letter = "F",
            accentColor = Color(0xFFAD9BFF),
            councilStyle = "Practical, concise and energetic. Convert decisions into steps, plans and deliverables.",
            promptPrefix = "Act as Friday, my execution specialist. Turn this into a practical plan and deliverable: "
        ),
        AiSpecialist(
            name = "Edith",
            role = "Research & context",
            description = "Researches carefully and separates evidence from assumptions.",
            letter = "E",
            accentColor = Color(0xFF65B9FF),
            councilStyle = "Evidence-led, curious, precise. Separate verified facts from assumptions.",
            promptPrefix = "Act as Edith, my research specialist. Explain the evidence, sources to verify and uncertainties about: "
        )
    )

    // Order in which the Five-AI Council executes (matches server/council.js)
    val councilOrder: List<AiSpecialist> = listOf(
        specialists.first { it.name == "Edith" },
        specialists.first { it.name == "Vision" },
        specialists.first { it.name == "Ultron" },
        specialists.first { it.name == "Friday" },
        specialists.first { it.name == "Jarvis" }
    )

    val quickPrompts: List<QuickPromptItem> = listOf(
        QuickPromptItem(
            title = "Research",
            subtitle = "Explore ideas and evidence",
            promptPrefix = "Research this topic and separate verified facts from assumptions: "
        ),
        QuickPromptItem(
            title = "Plan a project",
            subtitle = "Turn goals into milestones",
            promptPrefix = "Create a practical project plan with milestones for: "
        ),
        QuickPromptItem(
            title = "Write",
            subtitle = "Emails, proposals and reports",
            promptPrefix = "Draft a polished professional document about: "
        ),
        QuickPromptItem(
            title = "Analyze",
            subtitle = "Make better decisions",
            promptPrefix = "Analyze the options, risks and trade-offs for: "
        )
    )

    val workTemplates: List<WorkTemplateItem> = listOf(
        WorkTemplateItem(
            title = "Email & messages",
            subtitle = "Draft clear professional communication.",
            promptPrefix = "Create a polished professional email for this purpose: "
        ),
        WorkTemplateItem(
            title = "Project planning",
            subtitle = "Organize work from idea to delivery.",
            promptPrefix = "Create a detailed project plan with scope, milestones, risks and owners for: "
        ),
        WorkTemplateItem(
            title = "Business proposal",
            subtitle = "Shape a persuasive, practical proposal.",
            promptPrefix = "Prepare a structured business proposal for: "
        ),
        WorkTemplateItem(
            title = "Analysis",
            subtitle = "Find patterns and make decisions.",
            promptPrefix = "Analyze this data or problem and give findings, caveats and recommendations: "
        ),
        WorkTemplateItem(
            title = "Presentation",
            subtitle = "Build a clear slide-by-slide story.",
            promptPrefix = "Create a professional presentation outline about: "
        ),
        WorkTemplateItem(
            title = "Software planning",
            subtitle = "Define requirements and acceptance criteria.",
            promptPrefix = "Create a software requirements specification for: "
        )
    )

    val discoverTopics: List<DiscoverTopicItem> = listOf(
        DiscoverTopicItem(
            title = "Technology",
            subtitle = "AI · software · product building",
            promptPrefix = "Give me a briefing on emerging trends, key architectures, and practical opportunities in technology and AI product building: "
        ),
        DiscoverTopicItem(
            title = "Creative Studio",
            subtitle = "Design · writing · media",
            promptPrefix = "Help me brainstorm creative concepts, design direction, and compelling copy for: "
        ),
        DiscoverTopicItem(
            title = "Professional Growth",
            subtitle = "Careers · learning · business",
            promptPrefix = "Build a structured learning and career acceleration roadmap for: "
        ),
        DiscoverTopicItem(
            title = "Community",
            subtitle = "Ideas · conversations · projects",
            promptPrefix = "Suggest practical community project ideas and collaboration frameworks for: "
        )
    )

    fun specialistByName(name: String?): AiSpecialist? =
        specialists.firstOrNull { it.name.equals(name, ignoreCase = true) }
}
