package com.example.sportconnection;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sportconnection.model.Post;
import com.example.sportconnection.repository.PostRepository;
import com.example.sportconnection.utils.LoadingDialog;
import com.example.sportconnection.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

public class CreatePostActivity extends AppCompatActivity {
    private TextInputEditText editTextContent;
    private TextInputEditText editTextUrl;
    private Button btnPublish;
    private ImageButton btnClose;

    private PostRepository postRepository;
    private SessionManager sessionManager;
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        postRepository = new PostRepository();
        sessionManager = new SessionManager(this);
        loadingDialog = new LoadingDialog(this);

        initViews();
        setupListeners();
    }

    private void initViews() {
        editTextContent = findViewById(R.id.editTextContent);
        editTextUrl = findViewById(R.id.editTextUrl);
        btnPublish = findViewById(R.id.btnPublish);
        btnClose = findViewById(R.id.btnClose);
    }

    private void setupListeners() {
        btnClose.setOnClickListener(v -> finish());

        btnPublish.setOnClickListener(v -> publishPost());

        // Configurar manejo del teclado
        setupKeyboardHandling();
    }

    private void setupKeyboardHandling() {
        // Asegurar que los campos sean visibles cuando se enfocan
        if (editTextContent != null) {
            editTextContent.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    v.postDelayed(() -> {
                        v.getParent().requestChildFocus(v, v);
                    }, 200);
                }
            });
        }

        if (editTextUrl != null) {
            editTextUrl.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    v.postDelayed(() -> {
                        v.getParent().requestChildFocus(v, v);
                    }, 200);
                }
            });
        }
    }

    private void publishPost() {
        String content = editTextContent.getText() != null ?
                editTextContent.getText().toString().trim() : "";
        String url = editTextUrl.getText() != null ?
                editTextUrl.getText().toString().trim() : "";

        // Validar contenido
        if (content.isEmpty()) {
            Toast.makeText(this, "Escribe algo para publicar", Toast.LENGTH_SHORT).show();
            editTextContent.requestFocus();
            return;
        }

        // Obtener token
        String token = sessionManager.getToken();
        if (token == null) {
            Toast.makeText(this, "Debes iniciar sesión", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Mostrar loading
        loadingDialog.show("Publicando...");
        btnPublish.setEnabled(false);

        // Crear post
        postRepository.createPost(token, content, url.isEmpty() ? null : url,
                new PostRepository.CreatePostCallback() {
            @Override
            public void onSuccess(Post post) {
                loadingDialog.dismiss();
                Toast.makeText(CreatePostActivity.this,
                        "Publicación creada exitosamente", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onError(String message) {
                loadingDialog.dismiss();
                btnPublish.setEnabled(true);
                Toast.makeText(CreatePostActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
    }
}
