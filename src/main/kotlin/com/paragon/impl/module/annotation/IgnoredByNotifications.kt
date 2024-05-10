package com.paragon.impl.module.annotation

/**
 * This annotation is used to mark a module to be ignored by the notification system.
 * Examples: ClickGUI, HUDEditor
 *
 * @author Surge
 */
@Target(AnnotationTarget.CLASS)
annotation class IgnoredByNotifications