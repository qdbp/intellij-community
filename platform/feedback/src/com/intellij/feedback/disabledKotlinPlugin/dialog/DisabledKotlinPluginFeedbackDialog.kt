// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.feedback.disabledKotlinPlugin.dialog

import com.intellij.feedback.common.createFeedbackAgreementComponent
import com.intellij.feedback.common.dialog.CommonFeedbackSystemInfoData
import com.intellij.feedback.common.dialog.showFeedbackSystemInfoDialog
import com.intellij.feedback.disabledKotlinPlugin.bundle.DisabledKotlinPluginFeedbackBundle
import com.intellij.openapi.observable.properties.PropertyGraph
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.LicensingFacade
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import com.intellij.ui.components.TextComponentEmptyText
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.JBGaps
import com.intellij.util.ui.JBEmptyBorder
import com.intellij.util.ui.JBFont
import com.intellij.util.ui.JBUI
import kotlinx.serialization.json.Json
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.util.function.Predicate
import javax.swing.JComponent

class DisabledKotlinPluginFeedbackDialog(
  private val project: Project?,
  private val forTest: Boolean
) : DialogWrapper(project) {
  private val commonSystemInfoData: Lazy<CommonFeedbackSystemInfoData> = lazy { CommonFeedbackSystemInfoData.getCurrentData() }

  private val propertyGraph = PropertyGraph()

  private val developedUsingKotlin = propertyGraph.property(
    DisabledKotlinPluginFeedbackBundle.message("dialog.kotlin.feedback.question.1.checkbox.3.label"))

  private val checkBoxSlowsDownIDEProperty = propertyGraph.property(false)
  private val checkBoxBreaksCodeAnalysisProperty = propertyGraph.property(false)
  private val checkBoxMakeNoiseNotificationProperty = propertyGraph.property(false)
  private val checkBoxUsuallyDeactivatePluginsProperty = propertyGraph.property(false)
  private val checkBoxOtherProperty = propertyGraph.property(false)
  private val textFieldOtherProblemProperty = propertyGraph.property("")

  private val textAreaDetailExplainProperty = propertyGraph.property("")

  private val checkBoxEmailProperty = propertyGraph.property(false)
  private val textFieldEmailProperty = propertyGraph.lazyProperty { LicensingFacade.INSTANCE?.getLicenseeEmail().orEmpty() }

  private var checkBoxOther: JBCheckBox? = null
  private var checkBoxEmail: JBCheckBox? = null

  private val textFieldOtherColumnSize = 41
  private val textAreaRowSize = 4
  private val textAreaOverallFeedbackColumnSize = 42
  private val textFieldEmailColumnSize = 25

  private val jsonConverter = Json { prettyPrint = true }

  init {
    init()
    title = DisabledKotlinPluginFeedbackBundle.message("dialog.kotlin.feedback.top.title")
    isResizable = false
  }

  override fun doOKAction() {
    super.doOKAction()

  }

  override fun createCenterPanel(): JComponent? {
    val mainPanel = panel {
      row {
        label(DisabledKotlinPluginFeedbackBundle.message("dialog.kotlin.feedback.title")).applyToComponent {
          font = JBFont.h1()
        }
      }

      row {
        label(DisabledKotlinPluginFeedbackBundle.message("dialog.kotlin.feedback.description"))
      }.bottomGap(BottomGap.MEDIUM)

      buttonsGroup {
        row {
          radioButton(DisabledKotlinPluginFeedbackBundle.message("dialog.kotlin.feedback.question.1.checkbox.1.label"),
                      DisabledKotlinPluginFeedbackBundle.message("dialog.kotlin.feedback.question.1.checkbox.1.label"))
            .label(DisabledKotlinPluginFeedbackBundle.message("dialog.kotlin.feedback.question.1.title"), LabelPosition.TOP)
        }.topGap(TopGap.MEDIUM)
        row {
          radioButton(DisabledKotlinPluginFeedbackBundle.message("dialog.kotlin.feedback.question.1.checkbox.2.label"),
                      DisabledKotlinPluginFeedbackBundle.message("dialog.kotlin.feedback.question.1.checkbox.2.label"))
        }
        row {
          radioButton(DisabledKotlinPluginFeedbackBundle.message("dialog.kotlin.feedback.question.1.checkbox.3.label"),
                      DisabledKotlinPluginFeedbackBundle.message("dialog.kotlin.feedback.question.1.checkbox.3.label"))
        }.bottomGap(BottomGap.MEDIUM)
      }.bind({ developedUsingKotlin.get() }, { developedUsingKotlin.set(it) })

      row {
        checkBox(DisabledKotlinPluginFeedbackBundle.message("dialog.kotlin.feedback.question.2.checkbox.1.label"))
          .bindSelected(checkBoxSlowsDownIDEProperty)
          .label(DisabledKotlinPluginFeedbackBundle.message("dialog.kotlin.feedback.question.2.title"), LabelPosition.TOP)
      }.topGap(TopGap.MEDIUM)
      row {
        checkBox(DisabledKotlinPluginFeedbackBundle.message("dialog.kotlin.feedback.question.2.checkbox.2.label"))
          .bindSelected(checkBoxBreaksCodeAnalysisProperty)
      }
      row {
        checkBox(DisabledKotlinPluginFeedbackBundle.message("dialog.kotlin.feedback.question.2.checkbox.3.label"))
          .bindSelected(checkBoxMakeNoiseNotificationProperty)
      }
      row {
        checkBox(DisabledKotlinPluginFeedbackBundle.message("dialog.kotlin.feedback.question.2.checkbox.4.label"))
          .bindSelected(checkBoxUsuallyDeactivatePluginsProperty)
      }

      row {
        checkBox("").bindSelected(checkBoxOtherProperty).applyToComponent {
          checkBoxOther = this
        }.customize(JBGaps(right = 4))

        textField()
          .bindText(textFieldOtherProblemProperty)
          .columns(textFieldOtherColumnSize)
          .errorOnApply(DisabledKotlinPluginFeedbackBundle.message("dialog.kotlin.feedback.question.2.checkbox.5.required")) {
            checkBoxOtherProperty.get() && it.text.isBlank()
          }
          .applyToComponent {
            emptyText.text = DisabledKotlinPluginFeedbackBundle.message("dialog.kotlin.feedback.question.2.checkbox.5.placeholder")
            textFieldOtherProblemProperty.afterChange {
              if (it.isNotBlank()) {
                checkBoxOtherProperty.set(true)
              }
              else {
                checkBoxOtherProperty.set(false)
              }
            }
            putClientProperty(TextComponentEmptyText.STATUS_VISIBLE_FUNCTION,
                              Predicate<JBTextField> { textField -> textField.text.isEmpty() })
          }
      }.bottomGap(BottomGap.MEDIUM)

      row {
        textArea()
          .bindText(textAreaDetailExplainProperty)
          .rows(textAreaRowSize)
          .columns(textAreaOverallFeedbackColumnSize)
          .label(DisabledKotlinPluginFeedbackBundle.message("dialog.kotlin.feedback.textarea.label"), LabelPosition.TOP)
          .applyToComponent {
            wrapStyleWord = true
            lineWrap = true
            addKeyListener(object : KeyAdapter() {
              override fun keyPressed(e: KeyEvent) {
                if (e.keyCode == KeyEvent.VK_TAB) {
                  if ((e.modifiersEx and KeyEvent.SHIFT_DOWN_MASK) != 0) {
                    transferFocusBackward()
                  }
                  else {
                    transferFocus()
                  }
                  e.consume()
                }
              }
            })
          }
      }.bottomGap(BottomGap.MEDIUM).topGap(TopGap.SMALL)

      row {
        checkBox(DisabledKotlinPluginFeedbackBundle.message("dialog.kotlin.feedback.checkbox.email"))
          .bindSelected(checkBoxEmailProperty).applyToComponent {
            checkBoxEmail = this
          }
      }.topGap(TopGap.MEDIUM)
      indent {
        row {
          textField().bindText(textFieldEmailProperty).columns(textFieldEmailColumnSize).applyToComponent {
            emptyText.text = DisabledKotlinPluginFeedbackBundle.message("dialog.kotlin.feedback.textfield.email.placeholder")
            isEnabled = checkBoxEmailProperty.get()

            checkBoxEmail?.addActionListener { _ ->
              isEnabled = checkBoxEmailProperty.get()
            }
            putClientProperty(TextComponentEmptyText.STATUS_VISIBLE_FUNCTION,
                              Predicate<JBTextField> { textField -> textField.text.isEmpty() })
          }.errorOnApply(DisabledKotlinPluginFeedbackBundle.message("dialog.kotlin.feedback.checkbox.email.required")) {
            checkBoxEmailProperty.get() && it.text.isBlank()
          }.errorOnApply(DisabledKotlinPluginFeedbackBundle.message("dialog.kotlin.feedback.checkbox.email.invalid")) {
            checkBoxEmailProperty.get() && it.text.isNotBlank() && !it.text.matches(Regex(".+@.+\\..+"))
          }
        }.bottomGap(BottomGap.MEDIUM)
      }

      row {
        cell(createFeedbackAgreementComponent(project) {
          showFeedbackSystemInfoDialog(project, commonSystemInfoData.value)
        })
      }.bottomGap(BottomGap.SMALL).topGap(TopGap.MEDIUM)

    }.also { dialog ->
      dialog.border = JBEmptyBorder(JBUI.scale(15), JBUI.scale(10), JBUI.scale(0), JBUI.scale(10))
    }

    return JBScrollPane(mainPanel, JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER).apply {
      border = JBUI.Borders.empty()
    }
  }
}