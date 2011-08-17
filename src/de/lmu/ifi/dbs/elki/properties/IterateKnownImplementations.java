package de.lmu.ifi.dbs.elki.properties;
/*
This file is part of ELKI:
Environment for Developing KDD-Applications Supported by Index-Structures

Copyright (C) 2011
Ludwig-Maximilians-Universität München
Lehr- und Forschungseinheit für Datenbanksysteme
ELKI Development Team

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

import java.util.Iterator;
import java.util.regex.Pattern;

import de.lmu.ifi.dbs.elki.logging.Logging;
import de.lmu.ifi.dbs.elki.utilities.iterator.IterableIterator;

/**
 * Iterator over all instanciable classes of a given superclass/interface.
 * 
 * The list of "known" implementations is obtained via the ELKI properties
 * mechanism.
 * 
 * @author Erich Schubert
 * 
 * @apiviz.uses Properties oneway - - reads from
 */
public class IterateKnownImplementations implements IterableIterator<Class<?>> {
  /**
   * Logger
   */
  private static final Logging logger = Logging.getLogger(Properties.class);
  
  /**
   * Pattern to detect comments
   */
  private final static Pattern COMMENTS = Pattern.compile("^\\s*(#.*)?$");

  /**
   * Current class = next iterator value
   */
  Class<?> cur = null;

  /**
   * Index within the class names array
   */
  int index = 0;

  /**
   * Class names array
   */
  String[] classNames = null;

  /**
   * Super class
   */
  Class<?> superclass = null;

  /**
   * Constructor.
   * 
   * @param superclass Superclass to find implementations for.
   */
  public IterateKnownImplementations(Class<?> superclass) {
    this.superclass = superclass;
    this.classNames = Properties.ELKI_PROPERTIES.getProperty(superclass.getName());
    findNext();
  }

  /**
   * Find the next 'acceptable' result.
   */
  private void findNext() {
    if(classNames == null) {
      return;
    }
    cur = null;
    for(; index < classNames.length; index++) {
      String name = classNames[index];
      // skip commented classes.
      if (COMMENTS.matcher(name).matches()) {
        continue;
      }
      try {
        cur = Class.forName(name);
      }
      catch(ClassNotFoundException e) {
        logger.warning("Class " + name + " (from properties file) not found for superclass " + this.superclass.getName());
        continue;
      }
      if(!this.superclass.isAssignableFrom(cur)) {
        logger.warning("Class " + name + " (from properties file) is not a subclass of " + this.superclass.getName());
        continue;
      }
      // last iteration - matched!
      {
        index++;
        break;
      }
    }
  }

  /**
   * Return if there is a next known implementation
   */
  @Override
  public boolean hasNext() {
    return cur != null;
  }

  /**
   * Advance the iterator, returning the 'next' result
   */
  @Override
  public Class<?> next() {
    Class<?> ret = cur;
    findNext();
    return ret;
  }

  /**
   * Removals are not supported by this iterator.
   * 
   * @throws UnsupportedOperationException Not supported.
   */
  @Override
  public void remove() throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

  /**
   * Adapter for {@link java.lang.Iterable} interface for foreach statements
   */
  @Override
  public Iterator<Class<?>> iterator() {
    return this;
  }
}