package chemkin_model;

import datamodel.ModelValue;

/**
 * Abstract decorator for {@link AbstractCKPackager} in case other things are done
 * with the collection of chemkin simulations.s
 * @author Nick
 *
 */
public abstract class AbstractCKPackagerDecorator extends AbstractCKPackager {

	AbstractCKPackager packager;
	
	@Override
	public ModelValue[] getModelValues() {
		return packager.getModelValues();
	}
}
