package pl.burno.project2;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity
{
    private EditText emailEditText;
    private EditText subjectEditText;
    private EditText contentEditText;
    private PackageManager packageManager;
    private static final String gmailPackage = "com.google.android.gm";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUpView();

        packageManager = getPackageManager();

        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                String emailString = emailEditText.getText().toString();
                String subjectString = subjectEditText.getText().toString();
                String contentString = contentEditText.getText().toString();
                String validate = validate(emailString, subjectString, contentString);
                if (validate == null)
                {
                    Intent intent = getIntent(emailString, subjectString, contentString);

                    List<ResolveInfo> infos = packageManager.queryIntentActivities(intent, PackageManager.GET_RESOLVED_FILTER);
                    String gmailClassName = getGmailClassName(infos);

                    if (gmailClassName != null)
                        intent.setClassName(gmailPackage, gmailClassName);

                    if (infos.isEmpty())
                        Toast.makeText(MainActivity.this, getResources().getText(R.string.no_email_app), Toast.LENGTH_SHORT).show();
                    else
                        startActivity(Intent.createChooser(intent, getResources().getString(R.string.send_email)));
                } else
                    Toast.makeText(MainActivity.this, validate, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Nullable
    private String getGmailClassName(List<ResolveInfo> infos)
    {
        String gmailClassName = null;

        for (ResolveInfo info : infos)
        {
            ActivityInfo activityInfo = info.activityInfo;
            String activityPackageName = activityInfo.packageName;
            if (activityPackageName.contains(gmailPackage))
            {
                gmailClassName = activityInfo.name;
                break;
            }
        }
        return gmailClassName;
    }

    private Intent getIntent(String emailString, String subjectString, String contentString)
    {
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", emailString, null));
        intent.putExtra(Intent.EXTRA_SUBJECT, subjectString);
        intent.putExtra(Intent.EXTRA_TEXT, contentString);
        return intent;
    }

    private void setUpView()
    {
        emailEditText = (EditText) findViewById(R.id.emailEditText);
        subjectEditText = (EditText) findViewById(R.id.subjectEditText);
        contentEditText = (EditText) findViewById(R.id.contentEditText);
    }

    private String validate(String emailString, String subjectString, String contentString)
    {
        String messageString = null;

        if (emailString.isEmpty() || subjectString.isEmpty() || contentString.isEmpty())
            messageString = getResources().getString(R.string.empty);
        else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailString).matches())
            messageString = getResources().getString(R.string.wrong_email);
        return messageString;
    }
}
