package com.wismna.geoffroy.donext.domain.model

sealed class AppDestination(
    val route: String,
    val title: String,
    val showBackButton: Boolean = false,
) {
    data class TaskList(val taskListId: Long, val name: String) : AppDestination(
        route = "taskList/$taskListId",
        title = name,
    )

    object DueTodayList : AppDestination(
        route = "todayList",
        title = "Due Today",
        showBackButton = false,
    )
    object ManageLists : AppDestination(
        route = "manageLists",
        title = "Manage Lists",
        showBackButton = false,
    )
    object RecycleBin : AppDestination(
        route = "recycleBin",
        title = "Recycle Bin",
        showBackButton = false,
    )
}
