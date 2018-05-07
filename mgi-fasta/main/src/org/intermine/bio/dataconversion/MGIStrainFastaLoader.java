package org.intermine.bio.dataconversion;

import org.biojava.bio.Annotation;
import org.biojava.bio.seq.Sequence;

/**
 * A loader for FASTA files for the annotated strain genomes from ENSEMBL.
 */
public class MGIStrainFastaLoader extends FastaLoaderTask
{
    /**
     * {@inheritDoc}
     */
    @Override
    protected String getIdentifier(Sequence bioJavaSequence) {
        Annotation anno = bioJavaSequence.getAnnotation();
        String header = anno.getProperty("description_line").toString();
	// 6|A/J dna_rm:chromosome chromosome:A_J_v1:6:1:148608796:1 REF
	// 6|129S1/SvImJ dna_rm:chromosome chromosome:129S1_SvImJ_v1:6:1:154738780:1 REF
	return header.split("\\s")[0];
    }
}
