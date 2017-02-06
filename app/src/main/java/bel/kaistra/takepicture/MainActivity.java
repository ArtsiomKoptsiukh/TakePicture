package bel.kaistra.takepicture;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;


public class MainActivity extends AppCompatActivity implements ColorPickerDialog.OnColorChangedListener {
    private int PICK_IMAGE_REQUEST = 1;
    private static final int CAMERA_REQUEST = 1888;

    private Toolbar toolbarTop;
    private Toolbar toolbarBottom;
    private CustomView customView;

    private android.support.v4.view.ActionProvider shareActionProvider;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        customView = (CustomView) findViewById(R.id.custom_view);
        toolbarTop = (Toolbar) findViewById(R.id.toolbar_top);
        setSupportActionBar(toolbarTop);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbarBottom = (Toolbar) findViewById(R.id.toolbar_bottom);
        toolbarBottom.inflateMenu(R.menu.menu_drawing);
        toolbarBottom.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                handleDrawingIconTouched(item.getItemId());
                return false;
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                customView.setBackgroundDrawable(new BitmapDrawable(bitmap));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            customView.setBackgroundDrawable(new BitmapDrawable(photo));
        }
    }

    private void handleDrawingIconTouched(int itemId) {
        switch (itemId) {
            case R.id.action_delete:
                deleteDialog();
                break;
            case R.id.action_undo:
                customView.onClickUndo();
                break;
            case R.id.action_redo:
                customView.onClickRedo();
                break;
            case R.id.action_brush:
                changeBrushDialog();
                break;
            case R.id.action_color:
                changeColor();
                break;
            case R.id.action_getImage:
                getImgFromGallery();
                break;
            case R.id.action_getCameraImage:
                getImgFromCamera();
        }
    }

    private void getImgFromCamera() {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }

    private void getImgFromGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
        //customView.setCanvasBackground(bitmap);

    }

    private void logout() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.activity_facebook_login, null);
        //builder.setView(view);
        builder.setTitle("Выход из аккаунта Facebook");
        builder.setMessage("Вы действительно хотите выйти?");
        final Button logout = (Button) view.findViewById(R.id.login_button);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginManager.getInstance().logOut();
                Intent login = new Intent(MainActivity.this, FacebookLoginActivity.class);
                startActivity(login);
                finish();
            }
        });
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                logout.callOnClick();
                dialog.dismiss();
            }
        })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        builder.show();

    }

    private void logout(View view) {
        Button logout = (Button) view.findViewById(R.id.login_button);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginManager.getInstance().logOut();
                Intent login = new Intent(MainActivity.this, FacebookLoginActivity.class);
                startActivity(login);
                finish();
            }
        });
    }

    private void changeColor() {
        new ColorPickerDialog(this, this, 0xFFFF0000).show();

    }

    private void changeBrushDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_change_brush_size, null);
        builder.setView(view);

        final SeekBar seekBar = (SeekBar) view.findViewById(R.id.seek_bar_brush_size);
        seekBar.setMax(50);
        seekBar.setProgress((int) customView.getCurrentBrushSize());

        final TextView textViewBrushSize = (TextView) view.findViewById(R.id.text_view_brush_size);
        textViewBrushSize.setText(getResources().getString(R.string.brush_size) + " " +
                String.valueOf((int) Math.abs(customView.getCurrentBrushSize())));

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChanged = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChanged = progress;
                textViewBrushSize.setText(getResources().getString(R.string.brush_size) + " " + progressChanged);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //назначить тут размер кисти
                customView.setCurrentBrushSize(seekBar.getProgress());
                dialog.dismiss();
            }
        })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        builder.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem menuItem = menu.findItem(R.id.action_share);
        shareActionProvider = MenuItemCompat.getActionProvider(menuItem);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_share:
                shareDrawing();
                return true;
            case R.id.action_save:
                saveDrawing();
                return true;
            case R.id.action_facebook:
                logout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void saveDrawing() {
        android.app.AlertDialog.Builder saveDialog = new android.app.AlertDialog.Builder(this);
        saveDialog.setTitle("Сохранить");
        saveDialog.setMessage("Сохранить изображение на устройстве ?");
        saveDialog.setPositiveButton("Да", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //save drawing
                customView.setDrawingCacheEnabled(true);
                customView.invalidate();
                String path = Environment.getExternalStorageDirectory().toString();
                OutputStream fOut = null;
                File file = new File(path,
                        UUID.randomUUID().toString() + "test.png");
                file.getParentFile().mkdirs();

                try {
                    file.createNewFile();
                } catch (Exception e) {
                    Log.e("MainActivity", e.getCause() + e.getMessage());
                }

                try {
                    fOut = new FileOutputStream(file);
                } catch (Exception e) {
                    Log.e("MainActivity", e.getCause() + e.getMessage());
                }

                if (customView.getDrawingCache() == null) {
                    Log.e("MainActivity", "Unable to get drawing cache ");
                }

                customView.getDrawingCache()
                        .compress(Bitmap.CompressFormat.PNG, 85, fOut);
                Toast savedToast = Toast.makeText(getApplicationContext(),
                        "Изображение сохранено в " + path, Toast.LENGTH_LONG);
                savedToast.show();
                try {
                    fOut.flush();
                    fOut.close();
                } catch (IOException e) {
                    Log.e("MainActivity", e.getCause() + e.getMessage());
                }
            }
        });
        saveDialog.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        saveDialog.show();
    }

    private void deleteDialog() {
        AlertDialog.Builder deleteDialog = new AlertDialog.Builder(this);
        deleteDialog.setTitle(getString(R.string.delete_drawing));
        deleteDialog.setMessage(getString(R.string.new_drawing_warning));
        deleteDialog.setPositiveButton("Да", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                customView.eraseAll();
                dialog.dismiss();
            }
        });
        deleteDialog.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        deleteDialog.show();
    }

    private void shareDrawing() {
        customView.setDrawingCacheEnabled(true);
        customView.setWaterMark();
        customView.invalidate();
        String path = Environment.getExternalStorageDirectory().toString();
        OutputStream fOut = null;
        File file = new File(path,
                UUID.randomUUID().toString() + "test.png");
        file.getParentFile().mkdirs();

        try {
            file.createNewFile();
        } catch (Exception e) {
            Log.e("MainActivity", e.getCause() + e.getMessage());
        }

        try {
            fOut = new FileOutputStream(file);
        } catch (Exception e) {
            Log.e("MainActivity", e.getCause() + e.getMessage());
        }

        if (customView.getDrawingCache() == null) {
            Log.e("MainActivity", "Unable to get drawing cache ");
        }

        customView.getDrawingCache()
                .compress(Bitmap.CompressFormat.PNG, 85, fOut);

        try {
            fOut.flush();
            fOut.close();
            customView.deleteWatermark();
        } catch (IOException e) {
            Log.e("MainActivity", e.getCause() + e.getMessage());
        }

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        shareIntent.setType("image/png");
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, "Share image"));

    }


    @Override
    public void colorChanged(int color) {
        customView.setColor(color);
    }
}
