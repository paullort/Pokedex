package com.example.pokedex;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.

 * create an instance of this fragment.
 */
public class PokedexFragment extends Fragment {
    private static final String TAG = "PokedexFragment";
    private static final String BASE_URL = "https://pokeapi.co/api/v2/";

    private RequestQueue requestQueue;
    private GridView gridView;
    private PokemonAdapter adapter;
    private List<Pokemon> pokemonNames;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pokedex, container, false);

        // Inicializar la cola de solicitudes Volley
        requestQueue = Volley.newRequestQueue(requireContext());

        // Obtener la referencia al GridView desde el layout
        gridView = view.findViewById(R.id.gridView);

        // Realizar la solicitud para obtener la lista de nombres de Pokémon
        pokemonListRequest();

        return view;
    }


    private void pokemonListRequest() {
        String allPokemonUrl = BASE_URL + "pokemon?limit=151";

        StringRequest request = new StringRequest(
                Request.Method.GET,
                allPokemonUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            JSONArray results = jsonResponse.getJSONArray("results");
                            pokemonNames = new ArrayList<>();
                            for (int i = 0; i < results.length(); i++) {
                                JSONObject pokemonJSON = results.getJSONObject(i);
                                String name = pokemonJSON.getString("name");
                                String url = pokemonJSON.getString("url");
                                Pokemon pokemon = new Pokemon(i + 1, name, url);
                                pokemonNames.add(pokemon);
                                // Obtener los detalles del Pokémon para extraer las URL de las imágenes
                                pokemonDetailRequest(pokemon);
                            }

                            // Configurar el adaptador con la lista de nombres de Pokémon
                            adapter = new PokemonAdapter(requireContext(), pokemonNames);

                            // Vincular el adaptador al GridView
                            gridView.setAdapter(adapter);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e(TAG, "Error al analizar la respuesta JSON: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error en la solicitud: " + error.toString());
                    }
                }
        );

        // Agregar la solicitud a la cola de solicitudes
        requestQueue.add(request);
    }

    private void pokemonDetailRequest(final Pokemon pokemon) {
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                pokemon.getUrl_API(),
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject sprites = response.getJSONObject("sprites");
                            String defaultImageUrl = sprites.getString("front_default");
                            String shinyImageUrl = sprites.getString("front_shiny");
                            pokemon.setUrl_default(defaultImageUrl);
                            pokemon.setUrl_shiny(shinyImageUrl);
                            adapter.notifyDataSetChanged(); // Notificar al adaptador que los datos han cambiado
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e(TAG, "Error al analizar la respuesta JSON del detalle del Pokémon: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error en la solicitud del detalle del Pokémon: " + error.toString());
                    }
                }
        );

        // Agregar la solicitud a la cola de solicitudes
        requestQueue.add(request);
    }


}