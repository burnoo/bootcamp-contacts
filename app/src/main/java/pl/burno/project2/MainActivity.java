package pl.burno.project2;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity
{
    static final int PICK_CONTACT = 1;
    private static final String GMAIL_PACKAGE = "com.google.android.gm";
    private EditText emailEditText;
    private EditText subjectEditText;
    private EditText contentEditText;
    private PackageManager packageManager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        packageManager = getPackageManager();

        setUpView();

        checkDeepLink();

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == PICK_CONTACT)
        {
            if (resultCode == RESULT_OK)
            {
                emailEditText.setText(getEmailById(data.getData()));
            }
        }
    }

    private void pickEmailFormContacts()
    {
        Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Email.CONTENT_URI);
        contactPickerIntent.setType(ContactsContract.CommonDataKinds.Email.CONTENT_TYPE);
        startActivityForResult(contactPickerIntent, PICK_CONTACT);
    }

    private void checkDeepLink()
    {
        if (getIntent().getData() != null)
        {
            String name = getIntent().getData().getHost();
            name = name.toLowerCase();
            name = name.replaceAll("_", ".");
            emailEditText.setText(String.format(getResources().getString(R.string.droidsonroids_email), name));
            subjectEditText.requestFocus();
        }
    }

    private void sendEmail()
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
                intent.setClassName(GMAIL_PACKAGE, gmailClassName);

            if (infos.isEmpty())
                Toast.makeText(MainActivity.this, getResources().getText(R.string.no_email_app), Toast.LENGTH_SHORT).show();
            else
                startActivity(Intent.createChooser(intent, getResources().getString(R.string.send_email)));
        } else
            Toast.makeText(MainActivity.this, validate, Toast.LENGTH_SHORT).show();
    }

    private String validate(String emailString, String subjectString, String contentString)
    {
        String messageString = null;

        if (emailString.isEmpty() || subjectString.trim().isEmpty() || contentString.trim().isEmpty())
            messageString = getResources().getString(R.string.empty);
        else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailString).matches())
            messageString = getResources().getString(R.string.wrong_email);
        return messageString;
    }

    private String getGmailClassName(List<ResolveInfo> infos)
    {
        String gmailClassName = null;

        for (ResolveInfo info : infos)
        {
            ActivityInfo activityInfo = info.activityInfo;
            String activityPackageName = activityInfo.packageName;
            if (GMAIL_PACKAGE.equals(activityPackageName))
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

        findViewById(R.id.addEmailButton).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                pickEmailFormContacts();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        sendEmail();
        return super.onOptionsItemSelected(item);
    }

    public String getEmailById(Uri result)
    {
        Cursor cursorEmail = getContentResolver().query(result, null, null, null, null);
        cursorEmail.moveToFirst();
        return cursorEmail.getString(cursorEmail.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS));
    }

}
