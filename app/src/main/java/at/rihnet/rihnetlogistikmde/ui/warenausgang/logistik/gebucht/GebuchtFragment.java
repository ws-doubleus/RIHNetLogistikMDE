package at.rihnet.rihnetlogistikmde.ui.warenausgang.logistik.gebucht;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
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
import java.util.Optional;
import java.util.stream.Collectors;

import at.rihnet.rihnetlogistikmde.CommunicationSelectLine;
import at.rihnet.rihnetlogistikmde.R;
import at.rihnet.rihnetlogistikmde.databinding.FragmentWarenausgangGebuchtBinding;
import at.rihnet.rihnetlogistikmde.models.BelegInfo;
import at.rihnet.rihnetlogistikmde.models.Belegposition;
import at.rihnet.rihnetlogistikmde.models.Kategorie;
import at.rihnet.rihnetlogistikmde.models.Paket;
import at.rihnet.rihnetlogistikmde.models.SelectLine.DocumentCreated;
import at.rihnet.rihnetlogistikmde.models.SelectLine.DocumentJournalModelCreate;
import at.rihnet.rihnetlogistikmde.models.SelectLine.DocumentPositionReadModel;
import at.rihnet.rihnetlogistikmde.models.SelectLine.DocumentPositionStoreInformationUpdate;
import at.rihnet.rihnetlogistikmde.models.SelectLine.DocumentPrintInformation;
import at.rihnet.rihnetlogistikmde.models.SelectLine.JournalAttachmentCreated;
import at.rihnet.rihnetlogistikmde.models.SelectLine.JournalCreated;
import at.rihnet.rihnetlogistikmde.models.SelectLine.PredecessorDocumentData;
import at.rihnet.rihnetlogistikmde.models.SelectLine.PrintTarget;
import at.rihnet.rihnetlogistikmde.models.SelectLine.SuccessorsDocumentData;
import at.rihnet.rihnetlogistikmde.models.Verarbeitungskennzeichen;
import at.rihnet.rihnetlogistikmde.sqlite.LogDAO;
import at.rihnet.rihnetlogistikmde.sqlite.MyDatabase;
import at.rihnet.rihnetlogistikmde.ui.loading.LoadingDialogFragment;
import at.rihnet.rihnetlogistikmde.ui.warenausgang.logistik.LogistikViewModel;

public class GebuchtFragment extends Fragment {
    private final String TAG = "RIHNet";
    private FragmentWarenausgangGebuchtBinding binding;
    private RecyclerView rv_gebucht;
    private LogistikViewModel logistikViewModel;
    private SharedPreferences prefs;
    private LogDAO logDAO;
    private GebuchtRecyclerViewAdapter gebuchtRecyclerViewAdapter;
    private List<Belegposition> gebuchtList = new ArrayList<>();
    private LoadingDialogFragment loadingDialogFragment;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentWarenausgangGebuchtBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        loadingDialogFragment = LoadingDialogFragment.newInstance("Daten werden geladen...");
        loadingDialogFragment.setCancelable(false);
        loadingDialogFragment.show(requireActivity().getSupportFragmentManager(), "fragment_loading_dialog");

        initViewModel();
        initRoom();
        initSharedPreferences();
        initRecyclerView();
        initListener();
        initItemTouchHelper();

        if (loadingDialogFragment != null) {
            loadingDialogFragment.dismiss();
        }

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void initViewModel() {
        logistikViewModel = new ViewModelProvider(requireActivity()).get(LogistikViewModel.class);

        logistikViewModel.getGebucht().observe(getViewLifecycleOwner(), new Observer<>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onChanged(List<Belegposition> belegpositions) {
                gebuchtList = belegpositions;
                gebuchtRecyclerViewAdapter.setBelegpositionen(gebuchtList);
                gebuchtRecyclerViewAdapter.notifyDataSetChanged();
                updateEmptyView();
                logistikViewModel.setPakete(buildPakete(gebuchtList));
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

    private void initSharedPreferences() {
        prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
    }

    private void initRecyclerView() {
        rv_gebucht = binding.rvGebucht;
        gebuchtRecyclerViewAdapter = new GebuchtRecyclerViewAdapter(gebuchtList);
        rv_gebucht.setLayoutManager(new LinearLayoutManager(getContext()));
        rv_gebucht.setHasFixedSize(true);
        rv_gebucht.setItemAnimator(new DefaultItemAnimator());
        rv_gebucht.setAdapter(gebuchtRecyclerViewAdapter);
        updateEmptyView();

    }

    private void initListener() {
        gebuchtRecyclerViewAdapter.setOnItemClickListener(position -> {
            //TODO !!!

        });
    }

    private void initItemTouchHelper() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                handleSwipedItem(viewHolder.getAdapterPosition());
            }
        }).attachToRecyclerView(binding.rvGebucht);
    }

    private DocumentCreated handleDocumentSuccessor1(BelegInfo belegInfo) throws IOException {
        DocumentCreated documentCreated = null;

        for (int i = gebuchtList.size() - 1; i >= 0; i--) {
            List<DocumentPositionStoreInformationUpdate> positions = new ArrayList<>();
            Belegposition bp = gebuchtList.get(i);
            DocumentPositionStoreInformationUpdate dpsiu = new DocumentPositionStoreInformationUpdate();
            dpsiu.setIdentifier(bp.getKennung());
            dpsiu.setArticleNumber(bp.getArtikelnummer());
            dpsiu.setQuantity(bp.getMenge());
            if (bp.getSerieCharge().equals("S")) {
                dpsiu.setSerialNumber(bp.getSeriennummer());
            }
            positions.add(dpsiu);

            if (documentCreated == null) {
                // Neu anlegen
                SuccessorsDocumentData successorsDocumentData = new SuccessorsDocumentData();
                successorsDocumentData.setDocumentKindDestination("L");
                successorsDocumentData.setPositions(positions);
                documentCreated = CommunicationSelectLine.createDocumentSuccessor(
                        belegInfo.getBelegtyp() + belegInfo.getBelegnummer(),
                        successorsDocumentData
                );
                if (documentCreated != null) {
                    //TODO queueBelegpositionDAO.delete(bp);

                    // UI-Update: Entfernen aus ViewModel-Queue
                    requireActivity().runOnUiThread(() -> logistikViewModel.removeGebucht(bp));
                }

            } else {
                // Existierendes Document weiterführen
                PredecessorDocumentData predecessorDocumentData = new PredecessorDocumentData();
                predecessorDocumentData.setPositions(positions);
                boolean result = CommunicationSelectLine.belegUebernahme(
                        documentCreated.getDocumentKey(),
                        belegInfo.getBelegtyp() + belegInfo.getBelegnummer(),
                        predecessorDocumentData
                );
                if (result) {
                    //TODO queueBelegpositionDAO.delete(bp);

                    requireActivity().runOnUiThread(() -> logistikViewModel.removeGebucht(bp));
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
            //new Handler().postDelayed(() -> logistikViewModel.getExecutorService().execute(() -> {
                //TODO try catch
                DocumentJournalModelCreate djmc = new DocumentJournalModelCreate();
                djmc.setDate(LocalDate.now().toString());
                djmc.setContactKindIdentifier(1);
                djmc.setStatusId(7);
                djmc.setLabel("MDE Fotos");
                djmc.setText("Lieferschein: " + documentCreatedLieferschein.getDocumentNumber());
                JournalCreated journalCreated = null;
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
                            JournalAttachmentCreated jac = CommunicationSelectLine.addAttachment(journalCreated.getJournalIdentifier(), photo, "image/jpeg");
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
            //}), 300);
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
            //new Handler().postDelayed(() -> logistikViewModel.getExecutorService().execute(() -> {
                DocumentCreated documentCreatedRechnung = null;
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
            //}), 300);
        } else if (verarbeitungskennzeichen.getAnzahlLieferscheine() > 0) {
            druckeLieferschein(verarbeitungskennzeichen, documentCreated);
        } else {
            requireActivity().finish();
        }
    }

    private void druckeLieferschein(Verarbeitungskennzeichen verarbeitungskennzeichen, DocumentCreated documentCreated) {
        loadingDialogFragment = LoadingDialogFragment.newInstance("Lieferschein wird gedruckt...");
        loadingDialogFragment.setCancelable(false);
        loadingDialogFragment.show(requireActivity().getSupportFragmentManager(), "fragment_loading_dialog");
        //new Handler().postDelayed(() -> logistikViewModel.getExecutorService().execute(() -> {
            String lieferschein = prefs.getString("lieferschein", "");
            DocumentPrintInformation documentPrintInformation = new DocumentPrintInformation();
            documentPrintInformation.setNumberOfCopies(verarbeitungskennzeichen.getAnzahlLieferscheine());
            documentPrintInformation.setMasterName(lieferschein);
            documentPrintInformation.setPrintTarget(PrintTarget.File);
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
        //}), 300);
    }

    private void druckeRechnung(Verarbeitungskennzeichen verarbeitungskennzeichen, DocumentCreated documentCreatedRechnung, DocumentCreated documentCreatedLieferschein) {
        loadingDialogFragment = LoadingDialogFragment.newInstance("Rechnung wird gedruckt...");
        loadingDialogFragment.setCancelable(false);
        loadingDialogFragment.show(requireActivity().getSupportFragmentManager(), "fragment_loading_dialog");
        //new Handler().postDelayed(() -> logistikViewModel.getExecutorService().execute(() -> {
            String rechnung = prefs.getString("rechnung", "");
            DocumentPrintInformation documentPrintInformation = new DocumentPrintInformation();
            documentPrintInformation.setNumberOfCopies(verarbeitungskennzeichen.getAnzahlLieferscheine());
            documentPrintInformation.setMasterName(rechnung);
            documentPrintInformation.setPrintTarget(PrintTarget.File);
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
        //}), 300);
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
                handleAddJournalToDocumentWithLink(belegInfo, documentCreated);
            });
        });
    }

    private void updateEmptyView() {
        rv_gebucht = binding.rvGebucht;
        TextView tv_empty = binding.tvEmpty;
        //btn_lieferscheinanlegen = binding.btnLieferscheinanlegen;

        if (!gebuchtList.isEmpty()) {
            rv_gebucht.smoothScrollToPosition(gebuchtList.size() - 1);
            rv_gebucht.setVisibility(View.VISIBLE);
            tv_empty.setVisibility(View.GONE);
            //btn_lieferscheinanlegen.setVisibility(View.VISIBLE);
        } else {
            rv_gebucht.setVisibility(View.GONE);
            tv_empty.setVisibility(View.VISIBLE);
            //btn_lieferscheinanlegen.setVisibility(View.GONE);
        }
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

//    private void handleSwipedItem(int adapterPosition) {
//        Belegposition belegposition = gebuchtRecyclerViewAdapter.getBelegpositionAt(adapterPosition);
//
//        logistikViewModel.getExecutorService().execute(() -> {
//            //queueBelegpositionDAO.delete(belegposition);
//
//            requireActivity().runOnUiThread(() -> {
//                logistikViewModel.removeGebucht(belegposition);
//
//                // Offen in getBelegposition() anpassen
//                List<Belegposition> belegpositionen = logistikViewModel.getOffen().getValue();
//                if (belegpositionen != null) {
//                    Optional<Belegposition> gefundeneBelegposition = belegpositionen.stream()
//                            .filter(b -> b.getArtikelnummer().equals(belegposition.getArtikelnummer())
//                                    && b.getBelegnummer().equals(belegposition.getBelegnummer())
//                                    && b.getPostext().equals(belegposition.getPostext()))
//                            .findFirst();
//                    if (gefundeneBelegposition.isPresent()) {
//                        Belegposition bp = gefundeneBelegposition.get();
//                        bp.setOffen(bp.getOffen() + belegposition.getMenge());
//                        logistikViewModel.setOffen(belegpositionen);
//                    }else{
//                        belegposition.setMenge(belegposition.getOriginalMenge());
//                        //belegposition.setOffen(belegposition.getMenge());
//                        logistikViewModel.addOffen(belegposition);
//                    }
//                }
//            });
//        });
//    }

    private void handleSwipedItem(int adapterPosition) {
        Belegposition gebuchtPos   = gebuchtRecyclerViewAdapter.getBelegpositionAt(adapterPosition);
        int rueckMenge             = gebuchtPos.getMenge();      // tatsächlich stornierte Menge

        logistikViewModel.removeGebucht(gebuchtPos);             // erst aus „Gebucht“ löschen

        List<Belegposition> offen = logistikViewModel.getOffen().getValue();
        if (offen == null) return;

        // Versuch, eine noch vorhandene offene Zeile zu finden
        Optional<Belegposition> vorhanden = offen.stream()
                .filter(b -> b.getKennung().equals(gebuchtPos.getKennung()))
                .findFirst();

        if (vorhanden.isPresent()) {
            // Zeile existiert noch: Offen erhöhen
            Belegposition bp = vorhanden.get();
            bp.setOffen(bp.getOffen() + rueckMenge);
            logistikViewModel.setOffen(new ArrayList<>(offen));   // neues List‑Objekt -> LiveData triggert
        } else {
            // Zeile war ganz weg: neu anlegen
            Belegposition neu = gebuchtPos.clone();               // oder new Belegposition(...)
            neu.setOffen(rueckMenge);                             // **entscheidend**
            neu.setMenge(neu.getOriginalMenge());                 // optional, je nach Anzeige
            neu.setSeriennummer("---");                           // offene Positionen ohne SN
            logistikViewModel.addOffen(neu);
        }
    }

}