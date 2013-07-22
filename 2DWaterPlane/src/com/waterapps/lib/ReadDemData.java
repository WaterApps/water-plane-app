package com.waterapps.lib;

/**
 * Created by Steve on 7/21/13.
 */

import java.net.URI;

/**
 * An interface to read DEM data from files
 */
public interface ReadDemData {

    /**
     * Reads a DemData object from a specified file
     * @param fileUri File location to be read
     * @return DemData object read from file
     */
    public DemData readFromFile(URI fileUri);
}
