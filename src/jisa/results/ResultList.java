package jisa.results;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Stream;

public class ResultList extends ResultTable {

    private final List<Row> rows = new LinkedList<>();

    public interface ColumnBuilder {

        Column<?> create(String name, String unit);

    }

    public static ResultList copyOf(ResultTable original) {

        ResultList copy = new ResultList(original.getColumnsAsArray());
        original.getAttributes().forEach(copy::setAttribute);
        original.forEach(copy::addRow);
        return copy;

    }

    public static ResultList emptyCopyOf(ResultTable original) {

        ResultList copy = new ResultList(original.getColumnsAsArray());
        original.getAttributes().forEach(copy::setAttribute);
        return copy;

    }

    public static ResultList loadFile(String filePath) throws IOException {

        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String         header = reader.readLine();

        JSONObject attributes = null;

        if (header.startsWith("% ATTRIBUTES: ")) {
            attributes = new JSONObject(header.replaceFirst("% ATTRIBUTES: ", ""));
            header     = reader.readLine();
        }

        ResultList list = new ResultList(ResultTable.parseColumnHeaderLine(header));

        String line;
        while ((line = reader.readLine()) != null) {
            list.addRow(list.parseCSVLine(line));
        }

        if (attributes != null) {
            attributes.toMap().forEach((k, v) -> list.setAttribute(k, v.toString()));
        }

        return list;

    }

    public ResultList(Column... columns) {
        super(columns);
    }

    public ResultList(String... names) {
        this(Arrays.stream(names).map(DoubleColumn::new).toArray(DoubleColumn[]::new));
    }

    public ResultList(List<Column> columns) {
        this(columns.toArray(Column[]::new));
    }

    @Override
    public Row getRow(int index) {

        if (index < 0 || index >= rows.size()) {
            throw new IndexOutOfBoundsException("Row index out of bounds. 0 <= index <= " + (rows.size() - 1) + ".");
        }

        return rows.get(index);
    }

    @Override
    protected void addRowData(Row row) {
        rows.add(row);
    }

    @Override
    protected void clearData() {
        rows.clear();
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public Stream<Row> stream() {
        return rows.stream();
    }

    @Override
    public Iterator<Row> iterator() {
        return rows.iterator();
    }

    public static Collector<Row, ?, ResultList> collect(ResultTable source) {
        return collect(source.getColumnsAsArray());
    }

    public static Collector<Row, ?, ResultList> collect(Column... columns) {

        return new Collector<Row, ResultList, ResultList>() {

            @Override
            public Supplier<ResultList> supplier() {
                return () -> new ResultList(columns);
            }

            @Override
            public BiConsumer<ResultList, Row> accumulator() {
                return ResultTable::addRow;
            }

            @Override
            public BinaryOperator<ResultList> combiner() {

                return (left, right) -> {
                    right.forEach(left::addRow);
                    return left;
                };

            }

            @Override
            public Function<ResultList, ResultList> finisher() {
                return (r) -> r;
            }

            @Override
            public Set<Characteristics> characteristics() {
                return Collections.emptySet();
            }

        };

    }

}
