package stroom.proxy.util.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VersionSort {
    public static void main(String[] args) throws IOException {
        List<String> lines = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String line;
        while ((line = reader.readLine()) != null) {
            lines.add(line);
        }
        Collections.sort(lines, new VersionSortComparator());

        lines.forEach(System.out::println);
    }

}
