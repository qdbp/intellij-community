// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.feedback.npw

import com.intellij.feedback.common.FeedbackTypes
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class TestShowNewProjectFeedbackDialogAction : AnAction() {
  override fun actionPerformed(e: AnActionEvent) {
    FeedbackTypes.PROJECT_CREATION_FEEDBACK.showNotification(e.project, true)
  }
}