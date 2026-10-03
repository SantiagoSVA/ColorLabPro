package com.example.colorlabpro;

import android.os.Bundle;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private View root;
    private TextView tvHex;
    private Button btnFav, btnTheme;
    private GridLayout gridPalette;
    private ListView listFav;

    private String selectedHex = "#1E88E5";

    private SharedPreferences prefs;
    private static final String PREFS = "colorlab_prefs";
    private static final String KEY_THEME = "theme_color";
    private static final String KEY_FAVS = "favorites";

    private ArrayList<String> favList = new ArrayList<>();
    private ArrayAdapter<String> favAdapter;

    // Arreglo con los 24 colores de colors.xml
    private final String[] palette = {
            "#E53935", "#D81B60", "#8E24AA", "#5E35B1",
            "#1E88E5", "#00897B", "#43A047", "#C0CA33",
            "#FFB300", "#FB8C00", "#6D4C41", "#546E7A",
            "#3949AB", "#039BE5", "#00ACC1", "#7CB342",
            "#FDD835", "#F4511E", "#757575", "#212121",
            "#C2185B", "#00BFA5", "#FF8F00", "#4A148C"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        root = findViewById(R.id.root);
        tvHex = findViewById(R.id.tvHex);
        btnFav = findViewById(R.id.btnFav);
        btnTheme = findViewById(R.id.btnTheme);
        gridPalette = findViewById(R.id.gridPalette);
        listFav = findViewById(R.id.listFav);

        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);

        // 1) Cargar tema guardado
        selectedHex = prefs.getString(KEY_THEME, selectedHex);
        aplicarColorAUi(selectedHex);

        // 2) Cargar favoritos
        cargarFavoritos();
        favAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, favList);
        listFav.setAdapter(favAdapter);

        // 3) Crear paleta de colores
        crearPaleta();

        // 4) Guardar en favoritos
        btnFav.setOnClickListener(v -> agregarAFavoritos(selectedHex));

        // 5) Aplicar tema (guardar color principal)
        btnTheme.setOnClickListener(v -> {
            prefs.edit().putString(KEY_THEME, selectedHex).apply();
            Toast.makeText(this, "Tema guardado", Toast.LENGTH_SHORT).show();
        });

        // 6) Tocar favorito -> aplicarlo
        listFav.setOnItemClickListener((parent, view, position, id) -> {
            selectedHex = favList.get(position);
            aplicarColorAUi(selectedHex);
        });
    }

    private void crearPaleta() {
        gridPalette.removeAllViews();

        int size = (int) (getResources().getDisplayMetrics().density * 52); // Reducido ligeramente a 52dp para caber mejor
        int margin = (int) (getResources().getDisplayMetrics().density * 6); // 6dp

        for (String hex : palette) {
            View swatch = new View(this);
            swatch.setBackgroundColor(Color.parseColor(hex));

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = size;
            params.height = size;
            params.setMargins(margin, margin, margin, margin);
            swatch.setLayoutParams(params);

            swatch.setOnClickListener(v -> {
                selectedHex = hex;
                aplicarColorAUi(selectedHex);
            });

            gridPalette.addView(swatch);
        }
    }

    private void aplicarColorAUi(String hex) {
        tvHex.setText(hex);
        root.setBackgroundColor(Color.parseColor(hex));

        int textColor = esColorOscuro(hex) ? Color.WHITE : Color.BLACK;
        tvHex.setTextColor(textColor);
    }

    private boolean esColorOscuro(String hex) {
        int c = Color.parseColor(hex);
        int r = Color.red(c), g = Color.green(c), b = Color.blue(c);
        double luminancia = (0.299 * r + 0.587 * g + 0.114 * b);
        return luminancia < 140;
    }

    private void cargarFavoritos() {
        String favsString = prefs.getString(KEY_FAVS, "");
        favList.clear();
        if (!favsString.isEmpty()) {
            String[] items = favsString.split(",");
            favList.addAll(Arrays.asList(items));
        }
    }

    private void guardarFavoritos() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < favList.size(); i++) {
            sb.append(favList.get(i));
            if (i < favList.size() - 1) {
                sb.append(",");
            }
        }
        prefs.edit().putString(KEY_FAVS, sb.toString()).apply();
    }

    private void agregarAFavoritos(String hex) {
        if (!favList.contains(hex)) {
            favList.add(0, hex);
            if (favList.size() > 10) {
                favList.remove(favList.size() - 1);
            }
            guardarFavoritos();
            favAdapter.notifyDataSetChanged();
            Toast.makeText(this, "Añadido a favoritos", Toast.LENGTH_SHORT).show();
        }
    }
}