package dev.pivisolutions.dictus.companion.insertion;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * Lives in the separately installed test application, outside the production app UID/process.
 *
 * WHY Java: this activity runs in the test APK's own process, where the Kotlin standard library
 * is absent (AGP strips it from the test APK because the instrumented app already ships it).
 * Kotlin parameter null checks would crash here with NoClassDefFoundError on Intrinsics.
 */
public class ExternalEditorActivity extends Activity {
    public static final String EXTRA_FINISH = "dictus_fixture_finish";
    private static final String INITIAL_TEXT = "prefix 😀 suffix";

    private EditText editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().getBooleanExtra(EXTRA_FINISH, false)) {
            finish();
            return;
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        editor = new EditText(this);
        editor.setId(android.R.id.edit);
        editor.setContentDescription("external-companion-editor");
        editor.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        editor.setMinLines(4);
        resetEditor();
        setContentView(editor);
        editor.requestFocus();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && editor != null) {
            editor.post(() -> getSystemService(InputMethodManager.class)
                .showSoftInput(editor, InputMethodManager.SHOW_IMPLICIT));
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getBooleanExtra(EXTRA_FINISH, false)) {
            finish();
        } else if (editor != null) {
            resetEditor();
            editor.requestFocus();
        }
    }

    // Selection 7..9 covers the emoji's surrogate pair.
    private void resetEditor() {
        editor.setText(INITIAL_TEXT);
        editor.setSelection(7, 9);
    }
}
