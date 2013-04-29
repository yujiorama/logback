/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2013, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.core.joran.spi;

import java.util.ArrayList;
import java.util.List;

/**
 * A pattern is used to designate XML elements in a document.
 * 
 * <p>For more information see
 * http://logback.qos.ch/manual/onJoran.html#pattern
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class ElementSelector extends ElementPath {

  public ElementSelector() {
    super();
  }

  public ElementSelector(List<String> list) {
    super(list);
  }

  /**
   * Build an elementPath from a string.
   *
   * Note that "/x" is considered equivalent to "x" and to "x/"
   *
   */
  public ElementSelector(String p) {
    super(p);
  }

  public Object clone() {
    ElementSelector p = new ElementSelector();
    p.partList.addAll(this.partList);
    return p;
  }


  /**
   * Returns the number of "tail" components that this pattern has in common
   * with the pattern p passed as parameter. By "tail" components we mean the
   * components at the end of the pattern.
   */
  public int getTailMatchLength(ElementPath p) {
    if (p == null) {
      return 0;
    }

    int lSize = this.partList.size();
    int rSize = p.partList.size();

    // no match possible for empty sets
    if ((lSize == 0) || (rSize == 0)) {
      return 0;
    }

    int minLen = (lSize <= rSize) ? lSize : rSize;
    int match = 0;

    // loop from the end to the front
    for (int i = 1; i <= minLen; i++) {
      String l = (String) this.partList.get(lSize - i);
      String r = (String) p.partList.get(rSize - i);

      if (equalityCheck(l, r)) {
        match++;
      } else {
        break;
      }
    }
    return match;
  }

  public boolean isContainedIn(ElementPath p) {
    if(p == null) {
      return false;
    }
    return p.toStableString().contains(toStableString());
  }


  /**
   * Returns the number of "prefix" components that this pattern has in common
   * with the pattern p passed as parameter. By "prefix" components we mean the
   * components at the beginning of the pattern.
   */
  public int getPrefixMatchLength(ElementPath p) {
    if (p == null) {
      return 0;
    }

    int lSize = this.partList.size();
    int rSize = p.partList.size();

    // no match possible for empty sets
    if ((lSize == 0) || (rSize == 0)) {
      return 0;
    }

    int minLen = (lSize <= rSize) ? lSize : rSize;
    int match = 0;

    for (int i = 0; i < minLen; i++) {
      String l = (String) this.partList.get(i);
      String r = (String) p.partList.get(i);

      if (equalityCheck(l, r)) {
        match++;
      } else {
        break;
      }
    }

    return match;
  }

  private boolean equalityCheck(String x, String y) {
    return x.equalsIgnoreCase(y);
  }

  @Override
  public boolean equals(Object o) {
    if ((o == null) || !(o instanceof ElementSelector)) {
      return false;
    }

    ElementSelector r = (ElementSelector) o;

    if (r.size() != size()) {
      return false;
    }

    int len = size();

    for (int i = 0; i < len; i++) {
      if (!equalityCheck(get(i), r.get(i))) {
        return false;
      }
    }

    // if everything matches, then the two patterns are equal
    return true;
  }

  @Override
  public int hashCode() {
    int hc = 0;
    int len = size();

    for (int i = 0; i < len; i++) {
      // make Pattern comparisons case insensitive
      // http://jira.qos.ch/browse/LBCORE-76
      hc ^= get(i).toLowerCase().hashCode();
    }
    return hc;
  }


  public boolean fullPathMatch(ElementPath path) {
    if (path.size() != size()) {
      return false;
    }

    int len = size();
    for (int i = 0; i < len; i++) {
      if (!equalityCheck(get(i), path.get(i))) {
        return false;
      }
    }
    // if everything matches, then the two patterns are equal
    return true;
  }
}
