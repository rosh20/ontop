package org.semanticweb.ontop.owlrefplatform.core.resultset;

/*
 * #%L
 * ontop-reformulation-core
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.algebra.Extension;
import org.openrdf.query.algebra.ExtensionElem;
import org.openrdf.query.algebra.ProjectionElem;
import org.openrdf.query.algebra.ProjectionElemList;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;
import org.semanticweb.ontop.model.Constant;
import org.semanticweb.ontop.model.GraphResultSet;
import org.semanticweb.ontop.model.OBDADataFactory;
import org.semanticweb.ontop.model.OBDAException;
import org.semanticweb.ontop.model.ObjectConstant;
import org.semanticweb.ontop.model.TupleResultSet;
import org.semanticweb.ontop.model.URIConstant;
import org.semanticweb.ontop.model.ValueConstant;
import org.semanticweb.ontop.model.impl.BNodeConstantImpl;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.model.impl.OBDAVocabulary;
import org.semanticweb.ontop.ontology.*;
import org.semanticweb.ontop.ontology.impl.OntologyFactoryImpl;
import org.semanticweb.ontop.owlrefplatform.core.translator.SesameConstructTemplate;

//import com.hp.hpl.jena.sparql.syntax.Template;

public class QuestGraphResultSet implements GraphResultSet {

	private List<List<Assertion>> results = new ArrayList<List<Assertion>>();

	private TupleResultSet tupleResultSet;

//	private Template template;
	
	private SesameConstructTemplate sesameTemplate;

	List <ExtensionElem> extList = null;
	
	HashMap <String, ValueExpr> extMap = null;
	
	//store results in case of describe queries
	private boolean storeResults = false;

	private OBDADataFactory dfac = OBDADataFactoryImpl.getInstance();
	private OntologyFactory ofac = OntologyFactoryImpl.getInstance();

	public QuestGraphResultSet(TupleResultSet results, SesameConstructTemplate template,
			boolean storeResult) throws OBDAException {
		this.tupleResultSet = results;
		this.sesameTemplate = template;
		this.storeResults = storeResult;
		processResultSet(tupleResultSet, sesameTemplate);
	}

	@Override
	public TupleResultSet getTupleResultSet() {
		return tupleResultSet;
	}

	private void processResultSet(TupleResultSet resSet, SesameConstructTemplate template)
			throws OBDAException {
		if (storeResults) {
			//process current result set into local buffer, 
			//since additional results will be collected
			while (resSet.nextRow()) {
				this.results.add(processResults(resSet, template));
			}
		}
	}
	
	@Override
	public void addNewResultSet(List<Assertion> result)
	{
		results.add(result);
	}

//	@Override
//	public Template getTemplate() {
//		return template;
//	}

	
	/**
	 * The method to actually process the current result set Row.
	 * Construct a list of assertions from the current result set row.
	 * In case of describe it is called to process and store all 
	 * the results from a resultset.
	 * In case of construct it is called upon next, to process
	 * the only current result set.
	 */
	
	private List<Assertion> processResults(TupleResultSet result,
			SesameConstructTemplate template) throws OBDAException {
		List<Assertion> tripleAssertions = new ArrayList<Assertion>();
		List<ProjectionElemList> peLists = template.getProjectionElemList();
		
		Extension ex = template.getExtension();
		if (ex != null) 
			{
				extList = ex.getElements();
				HashMap <String, ValueExpr> newExtMap = new HashMap<String, ValueExpr>();
				for (int i = 0; i < extList.size(); i++) {
					newExtMap.put(extList.get(i).getName(), extList.get(i).getExpr());
				}
				extMap = newExtMap;
			}
		for (ProjectionElemList peList : peLists) {
		int size = peList.getElements().size();
		
		for (int i = 0; i < size / 3; i++) {
			
			Constant subjectConstant = getConstant(peList.getElements().get(i*3), result);
			Constant predicateConstant = getConstant(peList.getElements().get(i*3+1), result);
			Constant objectConstant = getConstant(peList.getElements().get(i*3+2), result);

			// Determines the type of assertion
			String predicateName = predicateConstant.getValue();
			if (predicateName.equals(OBDAVocabulary.RDF_TYPE)) {
				OClass concept = ofac.createClass(objectConstant.getValue());
				ClassAssertion ca = ofac.createClassAssertion(concept,
						(ObjectConstant) subjectConstant);
				tripleAssertions.add(ca);
			} else {
				if (objectConstant instanceof URIConstant) {
					PropertyExpression prop = ofac.createObjectProperty(predicateName);
					PropertyAssertion op = ofac
							.createPropertyAssertion(prop,
									(ObjectConstant) subjectConstant,
									(ObjectConstant) objectConstant);
					tripleAssertions.add(op);
				} else if (objectConstant instanceof BNodeConstantImpl) {
					PropertyExpression prop = ofac.createObjectProperty(predicateName);
					PropertyAssertion op = ofac
							.createPropertyAssertion(prop,
									(ObjectConstant) subjectConstant,
									(ObjectConstant) objectConstant);
					tripleAssertions.add(op);
				} else {
					PropertyExpression prop = ofac.createDataProperty(predicateName);
					PropertyAssertion dp = ofac
							.createPropertyAssertion(prop,
									(ObjectConstant) subjectConstant,
									(ValueConstant) objectConstant);
					tripleAssertions.add(dp);
				}
			}
		}
		}
		return (tripleAssertions);
	}
	
	@Override
	public boolean hasNext() throws OBDAException {
		//in case of describe, we return the collected results list information
		if (storeResults) {
			return results.size() != 0;
		} else {
			//in case of construct advance the result set cursor on hasNext
			return tupleResultSet.nextRow();
		}
	}

	@Override
	public List<Assertion> next() throws OBDAException {
		//if we collect results, then remove and return the next one in the list
		if (results.size() > 0) {
			return results.remove(0);
		} else {
			//otherwise we need to process the unstored result
			return processResults(tupleResultSet, sesameTemplate);
		}
	}

	private Constant getConstant(ProjectionElem node, TupleResultSet resSet)
			throws OBDAException {
		Constant constant = null;
		String node_name = node.getSourceName();
		ValueExpr ve = null;
		
		if (extMap!= null) {
			ve = extMap.get(node_name);
			if (ve!=null && ve instanceof Var)
				throw new RuntimeException ("Invalid query. Found unbound variable: "+ve);
		}
		
		if (node_name.charAt(0) == '-') {
			org.openrdf.query.algebra.ValueConstant vc = (org.openrdf.query.algebra.ValueConstant) ve;
			 if (vc.getValue() instanceof URIImpl) {
				 constant = dfac.getConstantURI(vc.getValue().stringValue());
			 } else if (vc.getValue() instanceof LiteralImpl) {
				 constant = dfac.getConstantLiteral(vc.getValue().stringValue());
			 } else {
				 constant = dfac.getConstantBNode(vc.getValue().stringValue());
			 }
		} else {
			constant = resSet.getConstant(node_name);
		}
		return constant;
	}
	
	@Override
	public void close() throws OBDAException {
		tupleResultSet.close();
	}

}