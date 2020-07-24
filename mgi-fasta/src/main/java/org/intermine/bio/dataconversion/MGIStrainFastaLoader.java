package org.intermine.bio.dataconversion;

import org.apache.log4j.Logger;
import org.biojava.nbio.core.sequence.template.Sequence;
import org.intermine.bio.dataconversion.FastaLoaderTask;

/**
 * A loader for FASTA files for the annotated strain genomes from ENSEMBL.
 */
public class MGIStrainFastaLoader extends FastaLoaderTask
{
    /**
     * {@inheritDoc}
     */

    private static final Logger LOG = Logger.getLogger(MGIStrainFastaLoader.class);

    @Override
    protected String getIdentifier(Sequence bioJavaSequence) {
        // from old library
        // Annotation anno = bioJavaSequence.getAnnotation();
        // String header = anno.getProperty("description_line").toString();
        // 6|A/J dna_rm:chromosome chromosome:A_J_v1:6:1:148608796:1 REF
        // 6|129S1/SvImJ dna_rm:chromosome chromosome:129S1_SvImJ_v1:6:1:154738780:1 REF
        //return header.split("\\s")[0];

        String header = bioJavaSequence.getAccession().getID();
        String name = null;

	name = header.split("\\s")[0];
        return name;
    }
}
