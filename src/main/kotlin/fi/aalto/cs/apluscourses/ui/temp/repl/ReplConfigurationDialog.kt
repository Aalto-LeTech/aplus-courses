package fi.aalto.cs.apluscourses.ui.temp.repl

import com.intellij.uiDesigner.core.GridConstraints
import javax.swing.JButton
import javax.swing.JPanel

class ReplConfigurationDialog
/**
 * Creates a REPL configuration dialog without a form (required later on by autogenerated code).
 */
    () {
    private val contentPane: JPanel? = null
    private val buttonOk: JButton? = null
    private val buttonCancel: JButton? = null
    private var replConfigurationForm: ReplConfigurationForm? = null
    private val form: JPanel? = null

    /**
     * Creates a REPL configuration dialog without a form (required later on by autogenerated code).
     */
    constructor(replConfigurationForm: ReplConfigurationForm) : this() {
        setReplConfigurationForm(replConfigurationForm)
    }

    fun setReplConfigurationForm(
        replConfigurationForm: ReplConfigurationForm
    ) {
        this.replConfigurationForm = replConfigurationForm
        replaceReplConfigurationFormWithIn(replConfigurationForm, form!!)
    }

    protected fun replaceReplConfigurationFormWithIn(
        replConfigurationForm: ReplConfigurationForm,
        jpanelForm: JPanel
    ) {
        //  this is copied from (IJ) autogenerated code
        val gridConstraints = GridConstraints(
            0, 0, 1, 1,
            GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
            GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW,
            GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW,
            null, null, null, 0, false
        )

        //  the only one in this particular implementation
        if (jpanelForm.getComponentCount() > 0) {
            jpanelForm.remove(0)
        }
        jpanelForm.add(replConfigurationForm.getContentPane(), gridConstraints)
        jpanelForm.revalidate()
        jpanelForm.repaint()
    }

    //  @Override
    fun onOk() {
        replConfigurationForm!!.updateModel()
        //    super.onOk();
    }

    //  @Override
    fun onCancel() {
        replConfigurationForm!!.cancelReplStart()
        //    super.onCancel();
    }

    fun getReplConfigurationForm(): ReplConfigurationForm {
        return replConfigurationForm!!
    }
}