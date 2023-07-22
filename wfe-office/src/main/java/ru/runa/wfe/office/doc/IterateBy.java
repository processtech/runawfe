package ru.runa.wfe.office.doc;

public enum IterateBy {
    indexes,
    numbers,
    items,
    keys,
    values;

    public static IterateBy identifyByString(DocxConfig config, String string) {
        for (IterateBy iterateBy : IterateBy.values()) {
            if (string.startsWith(iterateBy.name())) {
                return iterateBy;
            }
        }
        config.reportProblem("Invalid iterator found in '" + string + "'");
        return null;
    }
}
