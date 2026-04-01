package model;

public class Instrument {

    private final String symbol;
    private final String displayName;
    private final String instrumentKey;
    private final String exchange;
    private final String segment;
    private final boolean tradable;
    private final String instrumentType;

    public Instrument(String symbol, String displayName, String instrumentKey, String exchange, String segment, boolean tradable, String instrumentType) {
        this.symbol = symbol;
        this.displayName = displayName;
        this.instrumentKey = instrumentKey;
        this.exchange = exchange;
        this.segment = segment;
        this.tradable = tradable;
        this.instrumentType = instrumentType;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getInstrumentKey() {
        return instrumentKey;
    }

    public String getExchange() {
        return exchange;
    }

    public String getSegment() {
        return segment;
    }

    public boolean isTradable() {
        return tradable;
    }

    public String getInstrumentType() {
        return instrumentType;
    }

    public String toJson() {
        return "{"
                + "\"symbol\":\"" + escape(symbol) + "\","
                + "\"displayName\":\"" + escape(displayName) + "\","
                + "\"instrumentKey\":\"" + escape(instrumentKey) + "\","
                + "\"exchange\":\"" + escape(exchange) + "\","
                + "\"segment\":\"" + escape(segment) + "\","
                + "\"tradable\":" + tradable + ","
                + "\"instrumentType\":\"" + escape(instrumentType) + "\""
                + "}";
    }

    private String escape(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
