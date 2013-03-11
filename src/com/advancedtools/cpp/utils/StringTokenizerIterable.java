package com.advancedtools.cpp.utils;

import java.util.Iterator;
import java.util.StringTokenizer;
import java.io.File;

/**
 * User: maxim
 * Date: 31.05.2009
 * Time: 15:01:36
 */
public class StringTokenizerIterable implements Iterable<String> {
  private StringTokenizer tokenizer;
  public StringTokenizerIterable(String additionalIncludeDirs, String separator) {
    tokenizer = new StringTokenizer(additionalIncludeDirs, separator);
  }

  public Iterator<String> iterator() {
    return new Iterator<String>() {
      public boolean hasNext() {
        return tokenizer.hasMoreElements();
      }

      public String next() {
        return tokenizer.nextToken().replace(File.separatorChar, '/');
      }

      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }
}
