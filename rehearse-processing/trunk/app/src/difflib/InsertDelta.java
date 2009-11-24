/*
    Copyright 2009 Dmitry Naumenko (dm.naumenko@gmail.com)
    
    This file is part of Java Diff Utills Library.

    Java Diff Utills Library is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Java Diff Utills Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Java Diff Utills Library.  If not, see <http://www.gnu.org/licenses/>.
*/
package difflib;

import java.util.List;

/**
 * Describes the add-delta between original and revised texts.
 * 
 * @author <a href="dm.naumenko@gmail.com">Dmitry Naumenko</a>
 */
public class InsertDelta extends Delta {

	/**
	 * {@inheritDoc}
	 */
	public InsertDelta(Chunk original, Chunk revised) {
		super(original, revised);
	}

	/**
	 * {@inheritDoc}
	 * @throws PatchFailedException 
	 */
	@Override
	public void applyTo(List<Object> target) throws PatchFailedException {
		verify(target);
		int position = this.getOriginal().getPosition(); 
		List<?> lines = this.getRevised().getLines();
		for (int i = 0; i < lines.size(); i++) {
			target.add(position + i, lines.get(i));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void restore(List<Object> target) {
		int position = getRevised().getPosition();
		int size = getRevised().getSize();
		for (int i = 0; i < size; i++) {
			target.remove(position);
		}
	}

	@Override
	public void verify(List<?> target) throws PatchFailedException {
		if (getOriginal().getPosition() > target.size()) {
            throw new PatchFailedException("Incorrect patch for delta: " +
            		"delta original position > target size");
        }
		
	}

	@Override
	public String toString() {
		return "[InsertDelta, position: " + getOriginal().getPosition() + ", lines: " + 
			getRevised().getLines() + "]"; 
	}
}
