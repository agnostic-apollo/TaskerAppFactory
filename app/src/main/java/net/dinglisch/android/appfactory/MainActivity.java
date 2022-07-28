package net.dinglisch.android.appfactory;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import net.dinglisch.android.appfactory.utils.ApkTools;
import net.dinglisch.android.appfactory.utils.BootstrapInstaller;
import net.dinglisch.android.appfactory.utils.logger.Logger;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "MainActivity";

    private volatile boolean executing = false;


    Button run_button;
    EditText command_input_edit_text;
    TextView command_output_text_view;
    String mInputCommand ="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        command_input_edit_text = findViewById(R.id.edit_text);
        run_button = findViewById(R.id.button);
        command_output_text_view = findViewById(R.id.text_view);
        setCommandOutputTextViewText("Enter apk input and out file path separated with a space");

        run_button.setOnClickListener(v -> {
            mInputCommand = command_input_edit_text.getText().toString();
            setCommandOutputTextViewText("$ " + mInputCommand);
            runCommand();
        });

        BootstrapInstaller.installBootstrap(this);
    }

    private synchronized void runCommand() {
        String[] pathArgs = mInputCommand.split("\\s+");
        setCommandOutputTextViewText("");
       if (pathArgs.length != 2) {
           setCommandOutputTextViewText("Enter apk input and out file path separated with a space");
           return;
       }

       if (executing) {
           Logger.showToast(this, "Already executing command", false);
           return;
       }

       executing = true;

       new Thread() {
        @Override
        public void run() {
            StringBuilder commandMarkdownOutput = new StringBuilder();
            ApkTools.processApk(MainActivity.this, pathArgs[0], pathArgs[1], false, commandMarkdownOutput);
            runOnUiThread(() -> setCommandOutputTextViewText(commandMarkdownOutput.toString()));
            executing = false;
        }
       }.start();
    }

    private void setCommandOutputTextViewText(String text) {
        if (command_output_text_view != null)
            command_output_text_view.setText(text);
    }

    private void appendCommandOutputTextViewText(String text) {
        if (command_output_text_view != null)
            command_output_text_view.append(text);
    }

}
