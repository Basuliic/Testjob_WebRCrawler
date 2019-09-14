package testjobs;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import com.google.common.collect.UnmodifiableIterator;

public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    private static final String TEXT = "text";
    private static final String TYPE = "type";
    private static final String DEFAULT_TARGET_ELEMENT_ID = "make-everything-ok-button";
    private static final String CHARSET_NAME = "utf8";
    private static String targetElementId;

    /**
     * Analyzes HTML and finds a specific element based on similar characteristic os base html file element.
     *
     * @param args arguments for app:
     *             1 - sample html file
     *             2 - provided file
     *             3 - id of element to search
     */
    public static void main(String[] args) {
        String samplePath = args[0];
        String targetPath = args[1];
        targetElementId = args.length > 2 ? args[2] : DEFAULT_TARGET_ELEMENT_ID;

        Map<String, String> attributes = findAttributesForElementById(new File(samplePath));

        Optional<Element> foundElement = findElementByAttributes(new File(targetPath), attributes);

        foundElement.ifPresent(element -> LOGGER.info("XPath to found element: [{}]", buildXpathForElement(element)));
    }

    private static String buildXpathForElement(Element element) {
        StringBuilder xPath = new StringBuilder();
        Elements parents = element.parents();

        int nestingCount = parents.size() - 1;
        while (nestingCount >= 0) {
            Element parent = parents.get(nestingCount);
            xPath.append(">");
            xPath.append(parent.tagName());
            ///html/body/div/div/div[3]/div[1]/div/div[3]
            long count = parent.previousElementSiblings().stream().filter(el -> el.tagName().equals(parent.tagName())).count();
            xPath.append("[");
            xPath.append(count + 1);
            xPath.append("]");
            nestingCount--;
        }
        xPath.deleteCharAt(0);
        xPath.append(">").append(element.tagName());
        return xPath.toString();
    }

    private static Optional<Element> findElementByAttributes(File htmlFile, Map<String, String> attributes) {
        Optional<Element> optional = parseFileToElement(htmlFile);
        if (!optional.isPresent()) {
            return Optional.empty();
        }

        Element element = optional.get();
        HashMultiset<Element> elements = HashMultiset.create();
        elements.add(element.getElementById(targetElementId));
        String text = attributes.remove(TEXT);
        elements.addAll(element.getElementsMatchingText(text));
        String type = attributes.remove(TYPE);
        elements.addAll(element.getElementsByTag(type));
        //adding all elements with attributes with same value as in sample
        attributes.forEach((key, value) ->
                               elements.addAll(element.getElementsByAttribute(key)
                                                      .stream()
                                                      .filter(el -> el.attributes().get(key).equals(value))
                                                      .collect(Collectors.toList()))
                          );
        elements.remove(null);
        UnmodifiableIterator<Multiset.Entry<Element>> iterator = Multisets.copyHighestCountFirst(elements).entrySet().iterator();
        if (iterator.hasNext()) {
            Multiset.Entry<Element> entry = iterator.next();
            LOGGER.info("Chosen element have {} similarities with base element.", entry.getCount());
            return Optional.of(entry.getElement());
        } else {
            return Optional.empty();
        }
    }

    private static Map<String, String> findAttributesForElementById(File htmlFile) {
        Optional<Element> buttonOpt = parseFileToElement(htmlFile).map(element -> element.getElementById(targetElementId));
        if (!buttonOpt.isPresent()) {
            LOGGER.error("Couldn't find target element in provided sample file");
            throw new IllegalArgumentException("Can't find sample element in provided file");
        }
        Map<String, String> attributes = buttonOpt.get().attributes().asList()
                                                  .stream()
                                                  .collect(Collectors.toMap(Attribute::getKey, Attribute::getValue));
        attributes.put(TEXT, buttonOpt.get().text());
        attributes.put(TYPE, buttonOpt.get().tagName());
        return attributes;
    }

    private static Optional<Element> parseFileToElement(File htmlFile) {
        try {
            Document doc = Jsoup.parse(
                htmlFile,
                CHARSET_NAME,
                htmlFile.getAbsolutePath());

            return Optional.of(doc);

        } catch (IOException e) {
            LOGGER.error("Error reading [{}] file", htmlFile.getAbsolutePath(), e);
            return Optional.empty();
        }
    }
}
