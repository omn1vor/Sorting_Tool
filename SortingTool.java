package sorting;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class SortingTool<Type> {
    List<Type> data;
    String sortType;
    String dataTypeName;
    String inputFile;
    String outputFile;
    Scanner scanner;

    public SortingTool(String sortType, String inputFile, String outputFile) {
        this.sortType = sortType;
        this.inputFile = inputFile;
        this.outputFile = outputFile;
    }

    abstract void readInput();

    abstract int compare(Type a, Type b);

    abstract String naturalFormatString();
    abstract String byCountFormatString();

    public static SortingTool<?> getSorter(String[] args) {
        Map<String, String> params = parseArgs(args);
        String dataType = params.get("dataType");
        String sortType = params.get("sortType");
        String inputFile = params.get("inputFile");
        String outputFile = params.get("outputFile");

        if ("long".equals(dataType)) {
            return new LongSortingTool(sortType, inputFile, outputFile);
        } else if ("line".equals(dataType)) {
            return new LineSortingTool(sortType, inputFile, outputFile);
        } else if ("word".equals(dataType)) {
            return new WordSortingTool(sortType, inputFile, outputFile);
        } else {
            throw new IllegalArgumentException("No sorting type defined!");
        }
    }

    public void readData() {
        if (inputFile == null) {
            scanner = new Scanner(System.in);
        } else {
            try {
                String fileData = Files.readString(Paths.get(inputFile));
                scanner = new Scanner(fileData);
            } catch (IOException e) {
                throw new IllegalArgumentException(e.getMessage(), e);
            }
        }
        readInput();
    }

    public void printSort() {
        if ("byCount".equals(sortType)) {
            printCountSort();
        } else if ("natural".equals(sortType)) {
            printNaturalSort();
        } else {
            throw new IllegalArgumentException("No sorting type defined!");
        }
    }

    private static Map<String, String> parseArgs(String[] args) {
        String dataType = null;
        String sortType = null;
        String inputFile = null;
        String outputFile = null;

        Pattern pattern = Pattern.compile("-(\\w+) ([\\w.]*)");
        Matcher matcher = pattern.matcher(String.join(" ", args));

        while (matcher.find()) {
            String argName = matcher.group(1);
            String argValue = matcher.group(2);

            if ("dataType".equalsIgnoreCase(argName)) {
                if (argValue == null) {
                    throw new IllegalArgumentException("No data type defined!");
                } else {
                    dataType = argValue;
                }
            } else if ("sortingType".equalsIgnoreCase(argName)) {
                if (argValue == null) {
                    throw new IllegalArgumentException("No sorting type defined!");
                } else {
                    sortType = argValue;
                }
            } else if ("inputFile".equalsIgnoreCase(argName)) {
                if (argValue == null) {
                    throw new IllegalArgumentException("No file name defined!");
                } else {
                    inputFile = argValue;
                }
            } else if ("outputFile".equalsIgnoreCase(argName)) {
                if (argValue == null) {
                    throw new IllegalArgumentException("No file name defined!");
                } else {
                    outputFile = argValue;
                }
            } else {
                System.out.printf("\"%s\" is not a valid parameter. It will be skipped.%n", argName);
            }
        }

        if (dataType == null) {
            dataType = "word";
        }
        if (sortType == null) {
            sortType = "natural";
        }

        Map<String, String> params = new HashMap<>();
        params.put("dataType", dataType);
        params.put("sortType", sortType);
        if (inputFile != null) {
            params.put("inputFile", inputFile);
        }
        if (outputFile != null) {
            params.put("outputFile", outputFile);
        }

        return params;
    }

    void printCountSort() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Total %ss: %d%n", dataTypeName, data.size()));
        if (data.size() == 0) {
            return;
        }

        List<Value<Type>> grouped = new ArrayList<>();
        for (Type element : new HashSet<>(data)) {
            grouped.add(new Value<>(element, Collections.frequency(data, element)));
        }
        grouped.sort((a, b) -> {
            if (a.count == b.count) {
                return compare(a.value, b.value);
            } else {
                return Integer.compare(a.count, b.count);
            }
        });

        for (Value<Type> value : grouped) {
            sb.append(String.format(byCountFormatString(), value.value, value.count, value.count * 100 / data.size()));
        }
        if (outputFile == null) {
            System.out.println(sb);
        } else {
            try {
                Files.writeString(Paths.get(outputFile), sb);
            } catch (IOException e) {
                throw new IllegalArgumentException(e.getMessage(), e);
            }
        }
    }

    void printNaturalSort() {
        StringBuilder sb = new StringBuilder();
        data.sort(this::compare);
        sb.append(String.format("Total %ss: %d%n", dataTypeName, data.size()));
        sb.append("Sorted data: ");
        data.forEach(element -> sb.append(String.format(naturalFormatString(), element)));
        sb.append(System.lineSeparator());
        if (outputFile == null) {
            System.out.println(sb);
        } else {
            try {
                Files.writeString(Paths.get(outputFile), sb);
            } catch (IOException e) {
                throw new IllegalArgumentException(e.getMessage(), e);
            }
        }
    }

}

class LongSortingTool extends SortingTool<Long> {
    public LongSortingTool(String sortType, String inputFile, String outputFile) {
        super(sortType, inputFile, outputFile);
        data = new ArrayList<>();
        dataTypeName = "number";
    }

    @Override
    void readInput() {
        while (scanner.hasNext()) {
            String chunk = scanner.next();
            if (chunk.matches("[-+]?\\d+")) {
                data.add(Long.parseLong(chunk));
            } else {
                System.out.printf("\"%s\" is not a long. It will be skipped.%n", chunk);
            }
        }
    }

    @Override
    int compare(Long a, Long b) {
        return Long.compare(a, b);
    }

    @Override
    String naturalFormatString() {
        return "%d ";
    }

    @Override
    String byCountFormatString() {
        return "%d: %d time(s), %d%%%n";
    }
}

class LineSortingTool extends SortingTool<String> {
    public LineSortingTool(String sortType, String inputFile, String outputFile) {
        super(sortType, inputFile, outputFile);
        data = new ArrayList<>();
        dataTypeName = "line";
    }

    @Override
    void readInput() {
        while (scanner.hasNextLine()) {
            data.add(scanner.nextLine());
        }
    }

    @Override
    int compare(String a, String b) {
        return a.compareTo(b);
    }

    @Override
    String naturalFormatString() {
        return "%n%s";
    }

    @Override
    String byCountFormatString() {
        return "%s: %d time(s), %d%%%n";
    }
}

class WordSortingTool extends SortingTool<String> {
    public WordSortingTool(String sortType, String inputFile, String outputFile) {
        super(sortType, inputFile, outputFile);
        data = new ArrayList<>();
        dataTypeName = "word";
    }

    @Override
    public void readInput() {
        while (scanner.hasNext()) {
            data.add(scanner.next());
        }
    }

    @Override
    int compare(String a, String b) {
        return a.compareTo(b);
    }

    @Override
    String naturalFormatString() {
        return "%s ";
    }

    @Override
    String byCountFormatString() {
        return "%s: %d time(s), %d%%%n";
    }
}

class Value<Type> {
    Type value;
    int count;

    public Value(Type value, int count) {
        this.value = value;
        this.count = count;
    }
}