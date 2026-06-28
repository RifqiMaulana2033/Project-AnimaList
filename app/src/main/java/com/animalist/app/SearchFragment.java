package com.animalist.app;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchFragment extends Fragment {

    private AnimeAdapter adapter;
    private List<Anime> searchResults;

    private EditText etSearch;
    private Spinner spinnerGenre, spinnerStatus, spinnerSort;
    private Button btnApplyFilter;
    private RecyclerView rvSearch;
    private ShimmerFrameLayout shimmerView;

    private List<String> sortList;
    private ArrayAdapter<String> sortAdapter;

    private int currentPage = 1;
    private boolean isLoading = false;
    private boolean isUpdatingAdapter = false;

    private String currentOrderBy = "members";
    private String currentSortDirection = "desc";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        etSearch = view.findViewById(R.id.et_search_anime);
        spinnerGenre = view.findViewById(R.id.spinner_genre);
        spinnerStatus = view.findViewById(R.id.spinner_status);
        spinnerSort = view.findViewById(R.id.spinner_sort);
        btnApplyFilter = view.findViewById(R.id.btn_apply_filter);
        shimmerView = view.findViewById(R.id.shimmer_view_container);
        rvSearch = view.findViewById(R.id.rv_search_results);

        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        rvSearch.setLayoutManager(layoutManager);

        searchResults = new ArrayList<>();
        adapter = new AnimeAdapter(searchResults);
        rvSearch.setAdapter(adapter);

        setupSpinners();

        btnApplyFilter.setOnClickListener(v -> pencarianBaru());

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                pencarianBaru();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // KUNCI KEYBOARD: Pas pencet Enter (Kaca Pembesar)
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == android.view.KeyEvent.KEYCODE_ENTER)) {
                pencarianBaru();

                // Tutup Keyboard
                if (getContext() != null) {
                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
                }
                return true;
            }
            return false;
        });

        AdapterView.OnItemSelectedListener filterListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Cegah double pencarian pas ganti sort
                if (parent.getId() != R.id.spinner_sort) {
                    pencarianBaru();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

        spinnerGenre.setOnItemSelectedListener(filterListener);
        spinnerStatus.setOnItemSelectedListener(filterListener);

        // KUNCI LOGIKA SORTING DINAMIS
        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isUpdatingAdapter) return;

                String selectedSort = parent.getItemAtPosition(position).toString();

                if (selectedSort.equals(getString(R.string.sort_populer))) {
                    currentOrderBy = "members";
                    currentSortDirection = "desc";
                }
                else if (selectedSort.equals(getString(R.string.sort_rating_up))) {
                    currentOrderBy = "score";
                    currentSortDirection = "desc";
                    toggleItemText(position, getString(R.string.sort_rating_down));
                }
                else if (selectedSort.equals(getString(R.string.sort_rating_down))) {
                    currentOrderBy = "score";
                    currentSortDirection = "asc";
                    toggleItemText(position, getString(R.string.sort_rating_up));
                }
                else if (selectedSort.equals(getString(R.string.sort_newest))) {
                    currentOrderBy = "start_date";
                    currentSortDirection = "desc";
                    toggleItemText(position, getString(R.string.sort_oldest));
                }
                else if (selectedSort.equals(getString(R.string.sort_oldest))) {
                    currentOrderBy = "start_date";
                    currentSortDirection = "asc";
                    toggleItemText(position, getString(R.string.sort_newest));
                }
                else if (selectedSort.equals(getString(R.string.sort_az))) {
                    currentOrderBy = "title";
                    currentSortDirection = "asc";
                    toggleItemText(position, getString(R.string.sort_za));
                }
                else if (selectedSort.equals(getString(R.string.sort_za))) {
                    currentOrderBy = "title";
                    currentSortDirection = "desc";
                    toggleItemText(position, getString(R.string.sort_az));
                }

                pencarianBaru();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        rvSearch.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int pastVisibleItems = layoutManager.findFirstVisibleItemPosition();

                    if (!isLoading) {
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                            currentPage++;
                            tarikDataDariAPI(false);
                        }
                    }
                }
            }
        });

        pencarianBaru();
        return view;
    }

    private void toggleItemText(int position, String newText) {
        isUpdatingAdapter = true;
        sortList.set(position, newText);
        sortAdapter.notifyDataSetChanged();
        isUpdatingAdapter = false;
    }

    private void setupSpinners() {
        String[] genres = {"All", "Action", "Adventure", "Comedy", "Drama", "Fantasy", "Horror", "Isekai", "Mecha", "Mystery", "Psychological", "Romance", "Sci-Fi", "Slice of Life", "Sports", "Supernatural", "Thriller"};
        ArrayAdapter<String> genreAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, genres);
        spinnerGenre.setAdapter(genreAdapter);

        String[] statuses = {"All", "Airing", "Finished Airing", "Upcoming"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, statuses);
        spinnerStatus.setAdapter(statusAdapter);

        // Isi Menu Urutan pakai String Resource
        sortList = new ArrayList<>();
        sortList.add(getString(R.string.sort_populer));
        sortList.add(getString(R.string.sort_rating_up));
        sortList.add(getString(R.string.sort_newest));
        sortList.add(getString(R.string.sort_az));

        sortAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, sortList);
        spinnerSort.setAdapter(sortAdapter);
    }

    private void pencarianBaru() {
        currentPage = 1;
        rvSearch.setVisibility(View.GONE);
        shimmerView.setVisibility(View.VISIBLE);
        shimmerView.startShimmer();
        tarikDataDariAPI(true);
    }

    private void tarikDataDariAPI(boolean isReset) {
        isLoading = true;
        String queryTitle = etSearch.getText().toString().trim();
        String selectedGenre = spinnerGenre.getSelectedItem().toString();
        String selectedStatus = spinnerStatus.getSelectedItem().toString();

        String apiStatus = null;
        if (selectedStatus.equals("Airing")) apiStatus = "airing";
        else if (selectedStatus.equals("Finished Airing")) apiStatus = "complete";
        else if (selectedStatus.equals("Upcoming")) apiStatus = "upcoming";

        String apiGenre = null;
        switch (selectedGenre) {
            case "Action": apiGenre = "1"; break;
            case "Adventure": apiGenre = "2"; break;
            case "Comedy": apiGenre = "4"; break;
            case "Drama": apiGenre = "8"; break;
            case "Fantasy": apiGenre = "10"; break;
            case "Horror": apiGenre = "14"; break;
            case "Isekai": apiGenre = "62"; break;
            case "Mecha": apiGenre = "18"; break;
            case "Mystery": apiGenre = "7"; break;
            case "Psychological": apiGenre = "40"; break;
            case "Romance": apiGenre = "22"; break;
            case "Sci-Fi": apiGenre = "24"; break;
            case "Slice of Life": apiGenre = "36"; break;
            case "Sports": apiGenre = "30"; break;
            case "Supernatural": apiGenre = "37"; break;
            case "Thriller": apiGenre = "41"; break;
        }

        String orderBy = currentOrderBy;
        String sort = currentSortDirection;

        // Force Populer kalau query kosong biar list nggak aneh
        if (queryTitle.isEmpty() && orderBy.equals("members")) {
            orderBy = "members";
            sort = "desc";
        }
        String finalQuery = queryTitle.isEmpty() ? null : queryTitle;

        ApiClient.getService().searchAnime(finalQuery, apiStatus, apiGenre, orderBy, sort, 20, currentPage)
                .enqueue(new Callback<JikanResponse>() {
                    @Override
                    public void onResponse(Call<JikanResponse> call, Response<JikanResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            if (isReset) searchResults.clear();

                            for (JikanResponse.JikanAnime data : response.body().data) {
                                String title = data.title != null ? data.title : "Unknown";
                                String thumb = (data.images != null && data.images.jpg != null) ? data.images.jpg.imageUrl : "";
                                double rating = data.score != null ? data.score : 0.0;
                                String synopsis = data.synopsis != null ? data.synopsis : "Sinopsis tidak tersedia.";
                                String episodes = data.episodes != null ? data.episodes + " ep" : "? ep";
                                String year = data.year != null ? String.valueOf(data.year) : "?";
                                String status = data.status != null ? data.status : "Unknown";

                                List<String> genres = new ArrayList<>();
                                if (data.genres != null) {
                                    for (JikanResponse.JikanGenre g : data.genres) {
                                        genres.add(g.name);
                                    }
                                }
                                searchResults.add(new Anime(title, thumb, rating, genres, synopsis, episodes, year, status));
                            }
                            adapter.notifyDataSetChanged();

                            if (isReset) {
                                shimmerView.stopShimmer();
                                shimmerView.setVisibility(View.GONE);
                                rvSearch.setVisibility(View.VISIBLE);
                            }
                        }
                        isLoading = false;
                    }

                    @Override
                    public void onFailure(Call<JikanResponse> call, Throwable t) {
                        isLoading = false;
                        Log.e("API_ERROR", "Gagal narik data: " + t.getMessage());
                        if (getContext() != null) Toast.makeText(getContext(), "Gagal terhubung", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}