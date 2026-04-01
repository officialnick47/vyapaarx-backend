package service;

import connector.UpstoxConnector;
import model.Instrument;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class InstrumentsService {

    private static final ConcurrentHashMap<String, Instrument> BY_SYMBOL = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Instrument> BY_KEY = new ConcurrentHashMap<>();
    private static final AtomicLong LAST_SYNC_AT = new AtomicLong(0);
    private static volatile boolean SYNCED = false;
    private static volatile String LAST_SYNC_STATUS = "NOT_STARTED";

    public static void bootstrapDefaults() {
        addInstrument(new Instrument("NIFTY", "NIFTY 50", "NSE_INDEX|Nifty 50", "NSE", "INDEX", true, "INDEX"));
        addInstrument(new Instrument("BANKNIFTY", "NIFTY BANK", "NSE_INDEX|Nifty Bank", "NSE", "INDEX", true, "INDEX"));
        addInstrument(new Instrument("SENSEX", "SENSEX", "NSE_INDEX|SENSEX", "BSE", "INDEX", true, "INDEX"));
    }

    public static synchronized void syncMasterIfConfigured() {
        String masterUrl = System.getenv("UPSTOX_MASTER_URL");
        if (masterUrl == null || masterUrl.isBlank()) {
            LAST_SYNC_STATUS = "SKIPPED_NO_MASTER_URL";
            return;
        }

        try {
            String content = UpstoxConnector.fetchText(masterUrl);

            if (content == null || content.isBlank()) {
                LAST_SYNC_STATUS = "FAILED_EMPTY_MASTER";
                return;
            }

            parseAndLoadCsv(content);
            SYNCED = true;
            LAST_SYNC_AT.set(System.currentTimeMillis());
            LAST_SYNC_STATUS = "SUCCESS";

        } catch (Exception e) {
            LAST_SYNC_STATUS = "FAILED_" + e.getClass().getSimpleName();
        }
    }

    private static void parseAndLoadCsv(String csv) {
        String[] lines = csv.split("\\r?\\n");
        if (lines.length <= 1) return;

        int loaded = 0;

        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            if (line == null || line.isBlank()) continue;

            List<String> cols = parseCsvLine(line);
            if (cols.size() < 7) continue;

            String exchange = getSafe(cols, 0);
            String segment = getSafe(cols, 1);
            String symbol = normalizeSymbol(getSafe(cols, 2));
            String name = getSafe(cols, 3);
            String instrumentKey = getSafe(cols, 4);
            String instrumentType = getSafe(cols, 5);
            boolean tradable = "true".equalsIgnoreCase(getSafe(cols, 6)) || "1".equals(getSafe(cols, 6));

            if (symbol.isBlank() || instrumentKey.isBlank()) continue;

            Instrument instrument = new Instrument(
                    symbol,
                    name.isBlank() ? symbol : name,
                    instrumentKey,
                    exchange,
                    segment,
                    tradable,
                    instrumentType
            );

            addInstrument(instrument);
            loaded++;
        }

        LAST_SYNC_STATUS = "SUCCESS_LOADED_" + loaded;
    }

    private static List<String> parseCsvLine(String line) {
        List<String> cols = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes;
                continue;
            }

            if (c == ',' && !inQuotes) {
                cols.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }

        cols.add(current.toString().trim());
        return cols;
    }

    private static String getSafe(List<String> cols, int index) {
        if (index < 0 || index >= cols.size()) return "";
        return cols.get(index) == null ? "" : cols.get(index).trim();
    }

    private static void addInstrument(Instrument instrument) {
        BY_SYMBOL.put(instrument.getSymbol().toUpperCase(Locale.ROOT), instrument);
        BY_KEY.put(instrument.getInstrumentKey(), instrument);
    }

    public static Instrument getByInstrumentKey(String key) {
        return key == null ? null : BY_KEY.get(key);
    }

    public static List<String> resolveInstrumentKeys(List<String> symbols) {
        List<String> out = new ArrayList<>();
        for (String symbol : symbols) {
            if (symbol == null || symbol.isBlank()) continue;

            Instrument exact = BY_SYMBOL.get(normalizeSymbol(symbol));
            if (exact != null) {
                out.add(exact.getInstrumentKey());
                continue;
            }

            if (symbol.contains("|")) {
                out.add(symbol.trim());
            }
        }
        return out;
    }

    public static String search(String query, int limit) {
        String q = query == null ? "" : query.trim().toUpperCase(Locale.ROOT);
        List<Instrument> matches = new ArrayList<>();

        for (Instrument instrument : BY_SYMBOL.values()) {
            if (q.isBlank()
                    || instrument.getSymbol().contains(q)
                    || instrument.getDisplayName().toUpperCase(Locale.ROOT).contains(q)) {
                matches.add(instrument);
            }
        }

        matches.sort(Comparator.comparing(Instrument::getSymbol));

        StringBuilder sb = new StringBuilder();
        sb.append("{")
          .append("\"success\":true,")
          .append("\"timestamp\":").append(System.currentTimeMillis()).append(",")
          .append("\"count\":").append(Math.min(limit, matches.size())).append(",")
          .append("\"data\":[");

        int count = 0;
        for (Instrument instrument : matches) {
            if (count >= limit) break;
            if (count > 0) sb.append(",");
            sb.append(instrument.toJson());
            count++;
        }

        sb.append("],\"error\":null}");
        return sb.toString();
    }

    public static String getIndices() {
        List<Instrument> indices = new ArrayList<>();
        for (Instrument instrument : BY_SYMBOL.values()) {
            if ("INDEX".equalsIgnoreCase(instrument.getInstrumentType())) {
                indices.add(instrument);
            }
        }

        indices.sort(Comparator.comparing(Instrument::getSymbol));

        StringBuilder sb = new StringBuilder();
        sb.append("{")
          .append("\"success\":true,")
          .append("\"timestamp\":").append(System.currentTimeMillis()).append(",")
          .append("\"count\":").append(indices.size()).append(",")
          .append("\"data\":[");

        for (int i = 0; i < indices.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(indices.get(i).toJson());
        }

        sb.append("],\"error\":null}");
        return sb.toString();
    }

    public static String getSyncStatus() {
        return "{"
                + "\"success\":true,"
                + "\"timestamp\":" + System.currentTimeMillis() + ","
                + "\"data\":{"
                + "\"synced\":" + SYNCED + ","
                + "\"lastSyncAt\":" + LAST_SYNC_AT.get() + ","
                + "\"lastSyncStatus\":\"" + escape(LAST_SYNC_STATUS) + "\","
                + "\"instrumentCount\":" + BY_SYMBOL.size()
                + "},"
                + "\"error\":null"
                + "}";
    }

    public static String getHealthStatsJson() {
        return "{"
                + "\"synced\":" + SYNCED + ","
                + "\"lastSyncAt\":" + LAST_SYNC_AT.get() + ","
                + "\"lastSyncStatus\":\"" + escape(LAST_SYNC_STATUS) + "\","
                + "\"instrumentCount\":" + BY_SYMBOL.size()
                + "}";
    }

    private static String normalizeSymbol(String symbol) {
        return symbol == null ? "" : symbol.trim().toUpperCase(Locale.ROOT).replace(" ", "_");
    }

    private static String escape(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
