package at.rihnet.rihnetlogistikmde.ui.warenausgang.logistik.offen;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import at.rihnet.rihnetlogistikmde.CommunicationCommon;
import at.rihnet.rihnetlogistikmde.CommunicationSelectLine;
import at.rihnet.rihnetlogistikmde.CommunicationSql;
import at.rihnet.rihnetlogistikmde.R;
import at.rihnet.rihnetlogistikmde.audio.SoundPoolManager;
import at.rihnet.rihnetlogistikmde.databinding.FragmentWarenausgangOffenBinding;
import at.rihnet.rihnetlogistikmde.models.Artikel;
import at.rihnet.rihnetlogistikmde.models.BelegInfo;
import at.rihnet.rihnetlogistikmde.models.Belegposition;
import at.rihnet.rihnetlogistikmde.models.Kategorie;
import at.rihnet.rihnetlogistikmde.models.Paket;
import at.rihnet.rihnetlogistikmde.models.Paketanbindung;
import at.rihnet.rihnetlogistikmde.models.SelectLine.DocumentCreated;
import at.rihnet.rihnetlogistikmde.models.SelectLine.DocumentJournalModelCreate;
import at.rihnet.rihnetlogistikmde.models.SelectLine.DocumentPositionReadModel;
import at.rihnet.rihnetlogistikmde.models.SelectLine.DocumentPositionStoreInformationUpdate;
import at.rihnet.rihnetlogistikmde.models.SelectLine.DocumentPositionUpdateModel;
import at.rihnet.rihnetlogistikmde.models.SelectLine.DocumentPrintInformation;
import at.rihnet.rihnetlogistikmde.models.SelectLine.JournalCreated;
import at.rihnet.rihnetlogistikmde.models.SelectLine.PrintTarget;
import at.rihnet.rihnetlogistikmde.models.SelectLine.SuccessorsDocumentData;
import at.rihnet.rihnetlogistikmde.models.Verarbeitungskennzeichen;
import at.rihnet.rihnetlogistikmde.sqlite.LogDAO;
import at.rihnet.rihnetlogistikmde.sqlite.MyDatabase;
import at.rihnet.rihnetlogistikmde.ui.loading.LoadingDialogFragment;
import at.rihnet.rihnetlogistikmde.ui.warenausgang.logistik.LogistikViewModel;

public class OffenFragment extends Fragment implements MenuProvider {
    private final String TAG = "RIHNet";
    private FragmentWarenausgangOffenBinding binding;
    private LogistikViewModel logistikViewModel;
    private SharedPreferences prefs;
    private LogDAO logDAO;
    private OffenRecyclerViewAdapter offenRecyclerViewAdapter;
    private Button btn_pakete;
    private Button btn_lieferscheinanlegen;
    private List<Belegposition> offenList = new ArrayList<>();
    private List<Belegposition> gebuchtList = new ArrayList<>();
    private int successCounter = 0;
    private LoadingDialogFragment loadingDialogFragment;
    private OnSearchArtikel searchArtikel;
    private boolean paketanbindung;
    private boolean autoworker;

    public interface OnSearchArtikel {
        void onSearchArtikel(MenuItem menuItem, SearchView searchView);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentWarenausgangOffenBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        paketanbindung = prefs.getBoolean("paketanbindung", false);
        autoworker = prefs.getBoolean("autoworker", false);

        btn_pakete = binding.btnPakete;

        if (!paketanbindung && !autoworker) {
            btn_pakete.setVisibility(GONE);
        } else {
            btn_pakete.setVisibility(VISIBLE);
        }

        loadingDialogFragment = LoadingDialogFragment.newInstance("Daten werden geladen...");
        loadingDialogFragment.setCancelable(false);
        loadingDialogFragment.show(requireActivity().getSupportFragmentManager(), "fragment_loading_dialog");

        initViewModel();
        initRoom();
        initRecyclerView();
        initListener();
        loadData();

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().addMenuProvider(this, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        if (loadingDialogFragment != null && loadingDialogFragment.isAdded()) {
            loadingDialogFragment.dismissAllowingStateLoss();
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        searchArtikel = (OnSearchArtikel) context;
    }

    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.warenausgang_logistik_menu, menu);

        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) menuItem.getActionView();

        if (searchView != null) {
            EditText searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
            searchEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(18)});

            searchView.setQueryHint("Suchen...");
            searchArtikel.onSearchArtikel(menuItem, searchView);

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    if (query == null || query.trim().isEmpty()) return true;
                    logistikViewModel.setSearchArtikel(query.trim());
                    searchView.setQuery("", false);
                    searchView.clearFocus();
                    menuItem.collapseActionView();
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    return false;
                }
            });
        }
    }

    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
        return false;
    }

    private void initViewModel() {
        logistikViewModel = new ViewModelProvider(requireActivity()).get(LogistikViewModel.class);

        logistikViewModel.getOffen().observe(getViewLifecycleOwner(), new Observer<>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onChanged(List<Belegposition> belegpositions) {
                offenList = belegpositions;
                offenRecyclerViewAdapter.setBelegpositionen(offenList);
                offenRecyclerViewAdapter.notifyDataSetChanged();
                updateEmptyView();
            }
        });

        logistikViewModel.getGebucht().observe(getViewLifecycleOwner(), new Observer<>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onChanged(List<Belegposition> belegpositions) {
                gebuchtList = belegpositions;
                boolean nurKompltteBelegeVerarbeiten = prefs.getBoolean("belegeverarbeiten", false);
                if (nurKompltteBelegeVerarbeiten) {
                    btn_lieferscheinanlegen.setEnabled(offenList.isEmpty());
                } else {
                    btn_lieferscheinanlegen.setEnabled(!gebuchtList.isEmpty());
                }
                logistikViewModel.setPakete(buildPakete(gebuchtList));
            }
        });

        logistikViewModel.getSearchArtikel().observe(getViewLifecycleOwner(), this::doSearch);

        logistikViewModel.getPaketeAnzahl().observe(getViewLifecycleOwner(), integer -> {
            if (paketanbindung) {
                btn_pakete.setText(getString(R.string.btn_pakete, integer));
            }
            if (autoworker) {
                btn_pakete.setText(getString(R.string.btn_paket, integer));
            }
        });
    }

    private void initRoom() {
//        MyDatabase myDatabase = Room.databaseBuilder(requireContext(), MyDatabase.class, "rihnetdatabase")
//                .fallbackToDestructiveMigration()
//                .build();
        MyDatabase myDatabase = MyDatabase.getInstance(requireContext());
        logDAO = myDatabase.getLogDAO();
    }

    private void initRecyclerView() {
        RecyclerView rv_offen = binding.rvOffen;
        offenRecyclerViewAdapter = new OffenRecyclerViewAdapter(offenList);
        rv_offen.setLayoutManager(new LinearLayoutManager(getContext()));
        rv_offen.setHasFixedSize(true);
        rv_offen.setItemAnimator(new DefaultItemAnimator());
        rv_offen.setAdapter(offenRecyclerViewAdapter);
        updateEmptyView();
    }

    private void initListener() {
        offenRecyclerViewAdapter.setOnItemClickListener(position -> {
            Belegposition belegposition = offenRecyclerViewAdapter.getBelegpositionAt(position);
            belegposition.setPaketNummer(Optional.ofNullable(logistikViewModel.getPaketeAnzahl().getValue()).orElse(0));
            ArtikelDialogFragment artikelDialogFragment = ArtikelDialogFragment.newInstance(position, belegposition);
            artikelDialogFragment.show(getParentFragmentManager(), "artikelDialogFragment");
        });

        btn_pakete.setOnClickListener(v -> logistikViewModel.addPaketAnzahl());

        btn_pakete.setLongClickable(true);
        btn_pakete.setOnLongClickListener(v -> {
            if (paketanbindung) {
                logistikViewModel.setPaketeAnzahl(0);
            } else {
                logistikViewModel.setPaketeAnzahl(1);
            }
            return true;
        });

        btn_lieferscheinanlegen.setOnClickListener(v -> {
            loadingDialogFragment = LoadingDialogFragment.newInstance("Lieferschein wird angelegt...");
            loadingDialogFragment.setCancelable(false);
            loadingDialogFragment.show(requireActivity().getSupportFragmentManager(), "fragment_loading_dialog");

            logistikViewModel.getExecutorService().execute(() -> {
                int success;
                //int all = gebuchtList.size();
                int all = (int) gebuchtList.stream()
                        .map(Belegposition::getKennung)
                        .filter(Objects::nonNull)
                        .distinct()
                        .count();
                BelegInfo belegInfo = logistikViewModel.getBelegInfo().getValue();
                try {
                    if (belegInfo != null) {
                        String appKey = prefs.getString("appkey", "");
                        String baseAddress = prefs.getString("baseaddress", "");
                        String userName = prefs.getString("username", "");
                        String password = prefs.getString("password", "");

                        if (CommunicationSelectLine.login(appKey, baseAddress, userName, password)) {
                            android.util.Log.i(TAG, "Anmeldung erfolgreich!");

                            DocumentCreated documentCreated = handleDocumentSuccessor1(belegInfo);
                            success = this.successCounter;

                            // Im UI-Thread finalisieren (Log, DB-Aufräumungen, Dialog schließen etc.)
                            int finalSuccess = success;
                            requireActivity().runOnUiThread(() -> finalize(belegInfo, documentCreated, finalSuccess, all));
                        }
                    }
                } catch (Exception ex) {
                    Log.e(TAG, String.format("%s", ex.getMessage()));
                }
            });
        });
    }

    private void updateEmptyView() {
        RecyclerView rv_offen = binding.rvOffen;
        TextView tv_empty = binding.tvEmpty;
        btn_lieferscheinanlegen = binding.btnLieferscheinanlegen;

        if (!offenList.isEmpty()) {
            rv_offen.setVisibility(VISIBLE);
            tv_empty.setVisibility(GONE);
        } else {
            rv_offen.setVisibility(GONE);
            tv_empty.setVisibility(VISIBLE);
        }

        boolean nurKompltteBelegeVerarbeiten = prefs.getBoolean("belegeverarbeiten", false);
        if (nurKompltteBelegeVerarbeiten) {
            btn_lieferscheinanlegen.setEnabled(offenList.isEmpty());
        } else {
            btn_lieferscheinanlegen.setEnabled(!gebuchtList.isEmpty());
        }
    }

    private void loadData() {
        logistikViewModel.getExecutorService().execute(() -> {
            try {
                if (Objects.requireNonNull(logistikViewModel.getOffen().getValue()).isEmpty() && Objects.requireNonNull(logistikViewModel.getGebucht().getValue()).isEmpty()) {
                    BelegInfo belegInfo = logistikViewModel.getBelegInfo().getValue();
                    List<Belegposition> belegpositionen;
                    assert belegInfo != null;
                    belegpositionen = CommunicationSql.getBelegpByBelegtypBelegnummer0(belegInfo.getBelegtyp(), belegInfo.getBelegnummer());

                    requireActivity().runOnUiThread(() -> logistikViewModel.setOffen(belegpositionen));
                }
                if (loadingDialogFragment != null) {
                    loadingDialogFragment.dismiss();
                }
            } catch (Exception e) {
                Log.e(TAG, "Fehler beim Laden der Spinner-Daten (parallel): " + e.getMessage());
            }
        });
    }

    public static List<Paket> buildPakete(List<Belegposition> positionen) {

        if (positionen == null || positionen.isEmpty()) {
            return Collections.emptyList();
        }

        // 1) Positionen nach Paketnummer gruppieren
        Map<Integer, List<Belegposition>> byPaket = positionen.stream()
                .collect(Collectors.groupingBy(Belegposition::getPaketNummer));

        // 2) Für jedes Paket die Summen ermitteln und Paket‑Objekt erzeugen
        return byPaket.entrySet()
                .stream()
                .map(entry -> {
                    int paketNr = entry.getKey();
                    List<Belegposition> pos = entry.getValue();

                    // Gesamtgewicht
                    float gesGewicht = (float) pos.stream()
                            .mapToDouble(p -> p.getGewicht() * p.getMenge())
                            .sum();

                    // Gesamtwert
                    float gesWert = (float) pos.stream()
                            .mapToDouble(p -> p.getKalkulationspreis() * p.getMenge())
                            .sum();

                    Paket paket = new Paket(paketNr,
                            "Paket #" + paketNr,
                            gesGewicht,
                            gesWert);
                    paket.setPositionen(pos);
                    return paket;
                })
                .sorted(Comparator.comparingInt(Paket::getNummer))
                .collect(Collectors.toList());
    }

    private DocumentCreated handleDocumentSuccessor1(BelegInfo belegInfo) throws IOException {
        DocumentCreated documentCreated;
        SuccessorsDocumentData successorsDocumentData = new SuccessorsDocumentData();
        successorsDocumentData.setDocumentKindDestination("L");
        documentCreated = CommunicationSelectLine.createDocumentSuccessor(belegInfo.getBelegtyp() + belegInfo.getBelegnummer(), successorsDocumentData);
        if (documentCreated != null) {
            List<DocumentPositionReadModel> dprm = CommunicationSelectLine.readDocumentPositionByDocumentKey(documentCreated.getDocumentKey());
            assert dprm != null;
            for (int j = dprm.size() - 1; j >= 0; j--) {
                int finalJ = j;
                List<Belegposition> gebuchte = gebuchtList.stream().filter(f -> f.getKennung().equalsIgnoreCase(dprm.get(finalJ).getPredecessorIdentifier()) || !dprm.get(finalJ).isWarehouseArticle()).collect(Collectors.toList());
                if (gebuchte.isEmpty()) {
                    CommunicationSelectLine.deleteDocumentPositionByDocumentKey(documentCreated.getDocumentKey(), dprm.get(finalJ).getIdentifier());
                } else {
                    List<DocumentPositionStoreInformationUpdate> dpsiuList = new ArrayList<>();
                    int calculatedQuantityValue = 0;
                    for (Belegposition bp : gebuchte) {
                        DocumentPositionStoreInformationUpdate dpsiu = new DocumentPositionStoreInformationUpdate();
                        dpsiu.setIdentifier(dprm.get(finalJ).getIdentifier());
                        dpsiu.setQuantity(bp.getMenge());

                        if (bp.getSerieCharge().equals("S")) {
                            dpsiu.setSerialNumber(bp.getSeriennummer());
                        }
                        calculatedQuantityValue += bp.getMenge();
                        dpsiuList.add(dpsiu);
                    }
                    DocumentPositionUpdateModel dpum = new DocumentPositionUpdateModel();
                    dpum.setCalculatedQuantityValue(calculatedQuantityValue);
                    dpum.setStoreInformation(dpsiuList);
                    CommunicationSelectLine.updateDocumentPositionByDocumentKey(documentCreated.getDocumentKey(), dprm.get(finalJ).getIdentifier(), dpum);
                    boolean found = gebuchtList.stream()
                            .map(Belegposition::getKennung)
                            .anyMatch(k -> k.equalsIgnoreCase(dprm.get(finalJ).getPredecessorIdentifier()));
                    if (found) {
                        successCounter++;
                    }
                }


            }
        }
        return documentCreated;
    }

    private DocumentCreated handleDocumentSuccessor2(DocumentCreated documentCreatedLieferschein) throws IOException {
        DocumentCreated documentCreatedRechnung;
        List<DocumentPositionStoreInformationUpdate> positions = new ArrayList<>();
        List<DocumentPositionReadModel> dprm = CommunicationSelectLine.readDocumentPositionByDocumentKey(documentCreatedLieferschein.getDocumentKey());
        assert dprm != null;
        for (DocumentPositionReadModel item : dprm) {
            DocumentPositionStoreInformationUpdate docPos = new DocumentPositionStoreInformationUpdate();
            docPos.setIdentifier(item.getIdentifier());
            docPos.setArticleNumber(item.getArticleNumber());
            docPos.setQuantity(item.getQuantity());
            positions.add(docPos);
        }

        SuccessorsDocumentData successorsDocumentData = new SuccessorsDocumentData();
        successorsDocumentData.setDocumentKindDestination("R");
        successorsDocumentData.setPositions(positions);

        documentCreatedRechnung = CommunicationSelectLine.createDocumentSuccessor(
                documentCreatedLieferschein.getDocumentKey(),
                successorsDocumentData
        );

        return documentCreatedRechnung;
    }

    private void handleAddJournalToDocumentWithLink(BelegInfo belegInfo, DocumentCreated documentCreatedLieferschein) {
        List<File> photos = logistikViewModel.getPhotos().getValue();
        assert photos != null;
        if (!photos.isEmpty()) {
            loadingDialogFragment = LoadingDialogFragment.newInstance("Journal wird angelegt...");
            loadingDialogFragment.setCancelable(false);
            loadingDialogFragment.show(requireActivity().getSupportFragmentManager(), "fragment_loading_dialog");
            new Handler().postDelayed(() -> logistikViewModel.getExecutorService().execute(() -> {
                //TODO try catch
                DocumentJournalModelCreate djmc = new DocumentJournalModelCreate();
                djmc.setDate(LocalDate.now().toString());
                djmc.setContactKindIdentifier(1);
                djmc.setStatusId(7);
                djmc.setLabel("MDE Fotos");
                djmc.setText("Lieferschein: " + documentCreatedLieferschein.getDocumentNumber());
                JournalCreated journalCreated;
                try {
                    journalCreated = CommunicationSelectLine.addJournalToDocumentWithLink(documentCreatedLieferschein.getDocumentKey(), djmc);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                at.rihnet.rihnetlogistikmde.models.Log log;
                if (journalCreated != null && !journalCreated.getJournalIdentifier().isEmpty()) {
                    log = new at.rihnet.rihnetlogistikmde.models.Log(
                            "Lieferschein: " + documentCreatedLieferschein.getDocumentNumber()
                                    + "\nFotos: " + photos.size()
                                    + "\nJournal erfolgreich erstellt!",
                            ContextCompat.getColor(requireContext(), R.color.green_500),
                            Kategorie.WARENAUSGANG
                    );
                    for (File photo : photos) {
                        try {
                            CommunicationSelectLine.addAttachment(journalCreated.getJournalIdentifier(), photo, "image/jpeg");
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                } else {
                    log = new at.rihnet.rihnetlogistikmde.models.Log(
                            "Lieferschein: " + documentCreatedLieferschein.getDocumentNumber()
                                    + "\nFotos: 0"
                                    + "\nJournal konnte nicht erstellt!",
                            ContextCompat.getColor(requireContext(), R.color.red_500),
                            Kategorie.WARENAUSGANG
                    );

                }
                at.rihnet.rihnetlogistikmde.models.Log finalLog1 = log;
                requireActivity().runOnUiThread(() -> {
                    logistikViewModel.addLog(finalLog1);
                    if (loadingDialogFragment != null) {
                        loadingDialogFragment.dismiss();
                    }
                    handleVerarbeitungskennzeichen(belegInfo, documentCreatedLieferschein);
                });
                logDAO.insert(log);
            }), 300);
        } else {
            handleVerarbeitungskennzeichen(belegInfo, documentCreatedLieferschein);
        }
    }

    private void handleVerarbeitungskennzeichen(BelegInfo belegInfo, DocumentCreated documentCreated) {
        Verarbeitungskennzeichen verarbeitungskennzeichen = belegInfo.getVerarbeitungskennzeichen();
        if (verarbeitungskennzeichen.getId() == 0 || documentCreated == null) {
            requireActivity().finish();
            return;
        }

        // Rechnung erstellen
        if (verarbeitungskennzeichen.isAutoRechnung()) {
            loadingDialogFragment = LoadingDialogFragment.newInstance("Rechnung wird angelegt...");
            loadingDialogFragment.setCancelable(false);
            loadingDialogFragment.show(requireActivity().getSupportFragmentManager(), "fragment_loading_dialog");
            new Handler().postDelayed(() -> logistikViewModel.getExecutorService().execute(() -> {
                DocumentCreated documentCreatedRechnung;
                try {
                    documentCreatedRechnung = handleDocumentSuccessor2(documentCreated);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                at.rihnet.rihnetlogistikmde.models.Log log;
                if (documentCreatedRechnung != null && !documentCreatedRechnung.getDocumentNumber().isEmpty()) {
                    log = new at.rihnet.rihnetlogistikmde.models.Log(
                            "Lieferschein: " + documentCreated.getDocumentNumber()
                                    + "\nRechnung: " + documentCreatedRechnung.getDocumentNumber()
                                    + "\nRechnung erfolgreich erstellt!",
                            ContextCompat.getColor(requireContext(), R.color.green_500),
                            Kategorie.WARENAUSGANG
                    );
                } else {
                    log = new at.rihnet.rihnetlogistikmde.models.Log(
                            "Lieferschein: " + documentCreated.getDocumentNumber()
                                    + "\nRechnung: ---"
                                    + "\nRechnung konnte nicht erstellt werden!",
                            ContextCompat.getColor(requireContext(), R.color.orange_500),
                            Kategorie.WARENAUSGANG
                    );
                }
                DocumentCreated finalDocumentCreatedRechnung = documentCreatedRechnung;
                requireActivity().runOnUiThread(() -> {
                    logistikViewModel.addLog(log);
                    if (loadingDialogFragment != null) {
                        loadingDialogFragment.dismiss();
                    }
                    if (verarbeitungskennzeichen.getAnzahlRechnungen() > 0) {
                        druckeRechnung(verarbeitungskennzeichen, finalDocumentCreatedRechnung, documentCreated);
                    }
                });
                logDAO.insert(log);
            }), 300);
        } else if (verarbeitungskennzeichen.getAnzahlLieferscheine() > 0) {
            druckeLieferschein(verarbeitungskennzeichen, documentCreated);
        } else {
            requireActivity().finish();
        }
    }

    private void handlePaketanbindung(BelegInfo belegInfo, DocumentCreated documentCreatedLieferschein) {
        boolean paketanbindung = prefs.getBoolean("paketanbindung", false);
        Integer pakete = logistikViewModel.getPaketeAnzahl().getValue();
        if (pakete == null) {
            pakete = 0;
        }
        if (paketanbindung && (pakete > 0)) {
            int druckplatz = Integer.parseInt(prefs.getString("druckplatz", "1"));

            loadingDialogFragment = LoadingDialogFragment.newInstance("Paketanbindung wird angelegt...");
            loadingDialogFragment.setCancelable(false);
            loadingDialogFragment.show(requireActivity().getSupportFragmentManager(), "fragment_loading_dialog");
            Integer finalPakete = pakete;
            new Handler().postDelayed(() -> logistikViewModel.getExecutorService().execute(() -> {
                at.rihnet.rihnetlogistikmde.models.Log log;
                String userName = prefs.getString("username", "");
                Paketanbindung pa = new Paketanbindung(
                        documentCreatedLieferschein.getDocumentKind(),
                        documentCreatedLieferschein.getDocumentNumber(),
                        finalPakete,
                        druckplatz,
                        userName
                );
                int result = CommunicationSql.insertPaketanbindung(pa);
                if (result > 0) {
                    log = new at.rihnet.rihnetlogistikmde.models.Log(
                            "Lieferschein: " + documentCreatedLieferschein.getDocumentNumber()
                                    + "\nPakete: " + 1
                                    + "\nPaketanbindung erfolgreich angelegt!",
                            ContextCompat.getColor(requireContext(), R.color.green_500),
                            Kategorie.WARENAUSGANG
                    );
                } else {
                    log = new at.rihnet.rihnetlogistikmde.models.Log(
                            "Lieferschein: " + documentCreatedLieferschein.getDocumentNumber()
                                    + "\nPakete: " + 1
                                    + "\nPaketanbindung konnte nicht angelegt werden!",
                            ContextCompat.getColor(requireContext(), R.color.red_500),
                            Kategorie.WARENAUSGANG
                    );
                }
                requireActivity().runOnUiThread(() -> {
                    logistikViewModel.addLog(log);
                    if (loadingDialogFragment != null) {
                        loadingDialogFragment.dismiss();
                        handleAddJournalToDocumentWithLink(belegInfo, documentCreatedLieferschein);
                    }
                });
                logDAO.insert(log);
            }), 300);
        } else {
            handleAddJournalToDocumentWithLink(belegInfo, documentCreatedLieferschein);
        }
    }

    private void druckeLieferschein(Verarbeitungskennzeichen verarbeitungskennzeichen, DocumentCreated documentCreated) {
        loadingDialogFragment = LoadingDialogFragment.newInstance("Lieferschein wird gedruckt...");
        loadingDialogFragment.setCancelable(false);
        loadingDialogFragment.show(requireActivity().getSupportFragmentManager(), "fragment_loading_dialog");
        new Handler().postDelayed(() -> logistikViewModel.getExecutorService().execute(() -> {
            String lieferschein = prefs.getString("lieferschein", "");
            String druckausgabe = prefs.getString("druckausgabe", "Drucker");
            DocumentPrintInformation documentPrintInformation = new DocumentPrintInformation();
            documentPrintInformation.setNumberOfCopies(verarbeitungskennzeichen.getAnzahlLieferscheine());
            documentPrintInformation.setMasterName(lieferschein);
            if (druckausgabe.equals("Drucker")) {
                documentPrintInformation.setPrintTarget(PrintTarget.Printer);
            } else {
                documentPrintInformation.setPrintTarget(PrintTarget.File);
            }
            at.rihnet.rihnetlogistikmde.models.Log log;
            try {
                CommunicationSelectLine.documentPrint(documentCreated.getDocumentKey(), documentPrintInformation);
                log = new at.rihnet.rihnetlogistikmde.models.Log(
                        "Lieferschein: " + documentCreated.getDocumentNumber()
                                + "\nFile: " + documentCreated.getDocumentKey() + ".pdf"
                                + "\nLieferschein erfolgreich gedruckt!",
                        ContextCompat.getColor(requireContext(), R.color.green_500),
                        Kategorie.WARENAUSGANG
                );
                at.rihnet.rihnetlogistikmde.models.Log finalLog = log;
                requireActivity().runOnUiThread(() -> {
                    logistikViewModel.addLog(finalLog);
                    if (loadingDialogFragment != null) {
                        loadingDialogFragment.dismiss();
                    }
                    requireActivity().finish();
                });
            } catch (Exception ex) {
                Log.e(TAG, String.format("%s", ex.getMessage()));
                log = new at.rihnet.rihnetlogistikmde.models.Log(
                        "Lieferschein: " + documentCreated.getDocumentNumber()
                                + "\nFile: ---"
                                + "\nLieferschein konnte nicht gedruckt!",
                        ContextCompat.getColor(requireContext(), R.color.red_500),
                        Kategorie.WARENAUSGANG
                );
                at.rihnet.rihnetlogistikmde.models.Log finalLog1 = log;
                requireActivity().runOnUiThread(() -> {
                    logistikViewModel.addLog(finalLog1);
                    if (loadingDialogFragment != null) {
                        loadingDialogFragment.dismiss();
                    }
                    requireActivity().finish();
                });
            }
            logDAO.insert(log);
        }), 300);
    }

    private void druckeRechnung(Verarbeitungskennzeichen verarbeitungskennzeichen, DocumentCreated documentCreatedRechnung, DocumentCreated documentCreatedLieferschein) {
        loadingDialogFragment = LoadingDialogFragment.newInstance("Rechnung wird gedruckt...");
        loadingDialogFragment.setCancelable(false);
        loadingDialogFragment.show(requireActivity().getSupportFragmentManager(), "fragment_loading_dialog");
        new Handler().postDelayed(() -> logistikViewModel.getExecutorService().execute(() -> {
            String rechnung = prefs.getString("rechnung", "");
            String druckausgabe = prefs.getString("druckausgabe", "Drucker");
            DocumentPrintInformation documentPrintInformation = new DocumentPrintInformation();
            documentPrintInformation.setNumberOfCopies(verarbeitungskennzeichen.getAnzahlLieferscheine());
            documentPrintInformation.setMasterName(rechnung);
            if (druckausgabe.equals("Drucker")) {
                documentPrintInformation.setPrintTarget(PrintTarget.Printer);
            } else {
                documentPrintInformation.setPrintTarget(PrintTarget.File);
            }
            at.rihnet.rihnetlogistikmde.models.Log log;
            try {
                CommunicationSelectLine.documentPrint(documentCreatedRechnung.getDocumentKey(), documentPrintInformation);
                log = new at.rihnet.rihnetlogistikmde.models.Log(
                        "Rechnung: " + documentCreatedRechnung.getDocumentNumber()
                                + "\nFile: " + documentCreatedRechnung.getDocumentKey() + ".pdf"
                                + "\nRechnung erfolgreich gedruckt!",
                        ContextCompat.getColor(requireContext(), R.color.green_500),
                        Kategorie.WARENAUSGANG
                );
                at.rihnet.rihnetlogistikmde.models.Log finalLog = log;
                requireActivity().runOnUiThread(() -> {
                    logistikViewModel.addLog(finalLog);
                    if (loadingDialogFragment != null) {
                        loadingDialogFragment.dismiss();
                    }
                    if (verarbeitungskennzeichen.getAnzahlLieferscheine() > 0) {
                        druckeLieferschein(verarbeitungskennzeichen, documentCreatedLieferschein);
                    } else {
                        requireActivity().finish();
                    }
                });
            } catch (Exception ex) {
                Log.e(TAG, String.format("%s", ex.getMessage()));
                log = new at.rihnet.rihnetlogistikmde.models.Log(
                        "Rechnung: " + documentCreatedRechnung.getDocumentNumber()
                                + "\nFile: ---"
                                + "\nRechnung konnte nicht gedruckt!",
                        ContextCompat.getColor(requireContext(), R.color.red_500),
                        Kategorie.WARENAUSGANG
                );
                at.rihnet.rihnetlogistikmde.models.Log finalLog1 = log;
                requireActivity().runOnUiThread(() -> {
                    logistikViewModel.addLog(finalLog1);
                    if (loadingDialogFragment != null) {
                        loadingDialogFragment.dismiss();
                    }
                    requireActivity().finish();
                });
            }
            logDAO.insert(log);
        }), 300);
    }

    private void finalize(BelegInfo belegInfo, DocumentCreated documentCreated, int success, int all) {
        at.rihnet.rihnetlogistikmde.models.Log log;
        if (documentCreated != null && !documentCreated.getDocumentNumber().isEmpty()) {
            if (success == all) {
                log = new at.rihnet.rihnetlogistikmde.models.Log(
                        "Lieferschein: " + documentCreated.getDocumentNumber()
                                + "\nBelegpositionen: " + success + " / " + all
                                + "\nLieferschein erfolgreich erstellt!",
                        ContextCompat.getColor(requireContext(), R.color.green_500),
                        Kategorie.WARENAUSGANG
                );
            } else {
                log = new at.rihnet.rihnetlogistikmde.models.Log(
                        "Lieferschein: " + documentCreated.getDocumentNumber()
                                + "\nBelegpositionen: " + success + " / " + all
                                + "\nLieferschein teilweise erfolgreich erstellt!",
                        ContextCompat.getColor(requireContext(), R.color.orange_500),
                        Kategorie.WARENAUSGANG
                );
            }
        } else {
            log = new at.rihnet.rihnetlogistikmde.models.Log(
                    "Lieferschein: ---"
                            + "\nBelegpositionen: " + success + " / " + all
                            + "\nLieferschein konnte nicht erstellt werden!",
                    ContextCompat.getColor(requireContext(), R.color.red_500),
                    Kategorie.WARENAUSGANG
            );
        }

        // DB-Schreibvorgänge asynchron im Executor
        logistikViewModel.getExecutorService().execute(() -> {
            logDAO.insert(log);

            // Danach wieder ins UI
            requireActivity().runOnUiThread(() -> {
                logistikViewModel.addLog(log);
                if (loadingDialogFragment != null) {
                    loadingDialogFragment.dismiss();
                }
                //handleAddJournalToDocumentWithLink(belegInfo, documentCreated);
                handlePaketanbindung(belegInfo, documentCreated);
            });
        });
    }

    private void doSearch(String search) {
        CommunicationCommon.hideKeyboard(requireActivity());
        if (search == null || search.isEmpty()) return;

        if (isArtikelDialogVisible()) {
            Log.d(TAG, "Scan ignoriert, weil ArtikelDialogFragment geöffnet ist.");
            requireActivity().runOnUiThread(() -> logistikViewModel.setSearchArtikel(null));
            return;
        }

        logistikViewModel.getExecutorService().execute(() -> {
            if (offenList == null || offenList.isEmpty()) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Keine Belegpositionen verfügbar!", Toast.LENGTH_SHORT).show();
                    logistikViewModel.setSearchArtikel(null);
                });
                return;
            }

            String standort = prefs.getString("standort", "");
            boolean autoworker = prefs.getBoolean("autoworker", false);

            Artikel artikel = CommunicationSql.getArtikel(null, search, standort);
            if (artikel == null) {
                requireActivity().runOnUiThread(() -> {
                    SoundPoolManager.getInstance(requireContext()).playError();
                    Toast.makeText(getContext(), "Keinen passenden Artikel in Belegpositionen gefunden oder nichts mehr offen!", Toast.LENGTH_SHORT).show();
                    logistikViewModel.setSearchArtikel(null);
                });
                return;
            }

            Belegposition match = offenList.stream().filter(bp -> (bp.getArtikelnummer().equalsIgnoreCase(artikel.getArtikelnummer())) && bp.getOffen() > 0).findFirst().orElse(null);

            if (match == null) {
                // Kein passender Artikel gefunden -> Toast
                requireActivity().runOnUiThread(() -> {
                    SoundPoolManager.getInstance(requireContext()).playError();
                    Toast.makeText(getContext(), "Keinen passenden Artikel in Belegpositionen gefunden oder nichts mehr offen!", Toast.LENGTH_SHORT).show();
                    logistikViewModel.setSearchArtikel(null);
                });
                return;
            }

            // Bestand prüfen
            float bestand = CommunicationSql.getArtikelBestand(match.getArtikelnummer(), standort, match.getLager());
            int gebuchteMenge = Objects.requireNonNull(logistikViewModel.getGebucht().getValue()).stream().filter(bp -> match.getArtikelnummer().equals(bp.getArtikelnummer()))
                    .mapToInt(Belegposition::getMenge)
                    .sum();
            if ((bestand - gebuchteMenge) <= 0) {
                requireActivity().runOnUiThread(() -> {
                    SoundPoolManager.getInstance(requireContext()).playError();
                    Toast.makeText(getContext(), "Lagerbestand ist zu gering!", Toast.LENGTH_SHORT).show();
                    logistikViewModel.setSearchArtikel(null);
                });
                return;
            }

            if (artikel.getSerieCharge().equals("S") && !artikel.getSn().equals("S")) {
                int adapterPos = offenList.indexOf(match);    // -1 falls nicht gefunden
                if (adapterPos == -1) {
                    // sollte eigentlich nie passieren
                    return;
                }
                requireActivity().runOnUiThread(() -> {
                    match.setPaketNummer(Optional.ofNullable(logistikViewModel.getPaketeAnzahl().getValue()).orElse(0));
                    ArtikelDialogFragment artikelDialogFragment = ArtikelDialogFragment.newInstance(adapterPos, match);
                    artikelDialogFragment.show(getParentFragmentManager(), "artikelDialogFragment");
                });
                return;
            } else if (artikel.getSerieCharge().equals("S") && artikel.getSn().equals("S")) {
                // Püfen ob Seriennummer schon in Gebucht existiert!
                boolean found = Objects.requireNonNull(logistikViewModel.getGebucht().getValue()).stream().anyMatch(f -> f.getSeriennummer().equals(search));
                if (found) {
                    requireActivity().runOnUiThread(() -> {
                        SoundPoolManager.getInstance(requireContext()).playError();
                        Toast.makeText(getContext(), "Seriennummer: " + search + " wurde schon gebucht!", Toast.LENGTH_SHORT).show();
                        logistikViewModel.setSearchArtikel(null);
                    });
                    return;
                } else {
                    if (CommunicationSql.existsSerieCharge(artikel.getArtikelnummer(), search)) {
                        requireActivity().runOnUiThread(() -> {
                            SoundPoolManager.getInstance(requireContext()).playError();
                            Toast.makeText(getContext(), "Keine gültige Seriennummer!", Toast.LENGTH_SHORT).show();
                            logistikViewModel.setSearchArtikel(null);
                        });
                        return;
                    }
                }
            }

            // match.offen -= 1
            match.setOffen(match.getOffen() - 1);

            // (damit das UI den neuen "offen"-Wert bekommt)
            requireActivity().runOnUiThread(() -> {
                if (match.getOffen() <= 0) {
                    logistikViewModel.removeOffen(offenList.indexOf(match));
                } else {
                    logistikViewModel.setOffen(offenList);
                }
            });

            int paketNummer = 1;
            if (autoworker) {
                if (logistikViewModel.getPaketeAnzahl().getValue() != null) {
                    paketNummer = logistikViewModel.getPaketeAnzahl().getValue();
                }
            }
            Optional<Belegposition> belegpositionGebucht = Objects.requireNonNull(logistikViewModel.getGebucht().getValue()).stream().filter(f -> f.getKennung().equals(match.getKennung())).findFirst();
            if (belegpositionGebucht.isPresent() && !match.getSerieCharge().equals("S") && (match.getPaketNummer() == belegpositionGebucht.get().getPaketNummer())) {
                Belegposition bp = belegpositionGebucht.get();
                bp.setMenge(bp.getMenge() + 1);
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Artikel: " + bp.getArtikelnummer() + " | Menge: 1 wurde gebucht!", Toast.LENGTH_SHORT).show();
                    logistikViewModel.setSearchArtikel(null);
                });
            } else {
                Belegposition neueGebuchtPosition = match.clone();
                neueGebuchtPosition.setMenge(1);
                neueGebuchtPosition.setOffen(1);
                neueGebuchtPosition.setSeriennummer("---");
                neueGebuchtPosition.setPaketNummer(Optional.ofNullable(logistikViewModel.getPaketeAnzahl().getValue()).orElse(0));

                neueGebuchtPosition.setPaketNummer(paketNummer);
                if (artikel.getSn().equals("S")) {
                    neueGebuchtPosition.setSeriennummer(search);
                }
                requireActivity().runOnUiThread(() -> {
                    logistikViewModel.addGebucht(neueGebuchtPosition);
                    Toast.makeText(getContext(), "Artikel: " + neueGebuchtPosition.getArtikelnummer() + " | Menge: 1 wurde gebucht!", Toast.LENGTH_SHORT).show();
                    logistikViewModel.setSearchArtikel(null);

                });
            }
        });
    }

    private boolean isArtikelDialogVisible() {
        Fragment frag = getParentFragmentManager().findFragmentByTag("artikelDialogFragment");
        if (frag instanceof DialogFragment) {
            Dialog d = ((DialogFragment) frag).getDialog();
            return d != null && d.isShowing();
        }
        return false;
    }
}