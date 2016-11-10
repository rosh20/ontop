package it.unibz.krdb.obda.parser;

/*
 * #%L
 * ontop-obdalib-core
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

import it.unibz.krdb.obda.io.PrefixManager;
import it.unibz.krdb.obda.io.SimplePrefixManager;
import it.unibz.krdb.obda.model.Function;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;


/**
 * Test syntax of the parser.
 * Added new extension. Define if the mapping column contains a data property with rdfs:Literal ("{column}")
 * or an object property (<{column}>)
 * @link {it.unibz.krdb.obda.parser.TurtleOBDA.g}
 * */

public class TurtleSyntaxParserTest  {

	final static Logger log = LoggerFactory.getLogger(TurtleSyntaxParserTest.class);

	@Test
	public void test_1_1() {
		final boolean result = parse(":Person-{id} a :Person .");
		assertTrue(result);
	}

	@Test
	public void test_1_2() {
		final boolean result = parse("<http://example.org/testcase#Person-{id}> a :Person .");
		assertTrue(result);
	}

	@Test
	public void test_1_3() {
		final boolean result = parse("<http://example.org/testcase#Person-{id}> a <http://example.org/testcase#Person> .");
		assertTrue(result);
	}

	@Test
	public void test_1_4() {
		final boolean result = parse("<http://example.org/testcase#Person-{id}> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://example.org/testcase#Person> .");
		assertTrue(result);
	}

	@Test
	public void test_2_1() {
		final boolean result = parse(":Person-{id} :hasFather :Person-{id} .");
		assertTrue(result);
	}

	@Test
	public void test_2_2() {
		final boolean result = parse(":Person-{id} :hasFather <http://example.org/testcase#Person-12> .");
		assertTrue(result);
	}

	@Test
	public void test_2_3() {
		final boolean result = parse(":Person-{id} <http://example.org/testcase#hasFather> <http://example.org/testcase#Person-12> .");
		assertTrue(result);
	}

	@Test
	public void test_3_1_database() {
		final boolean result = parse(":Person-{id} :firstName {fname} .");
		assertTrue(result);
	}

	@Test
	public void test_3_1_new_literal() {
		final boolean result = parse(":Person-{id} :firstName \"{fname}\" .");
		assertTrue(result);
	}

	@Test
	public void test_3_1_new_string() {
		final boolean result = parse(":Person-{id} :firstName \"{fname}\"^^xsd:string .");
		assertTrue(result);
	}

	@Test
	public void test_3_1_new_iri() {
		final boolean result = parse(":Person-{id} :firstName <{fname}> .");
		assertTrue(result);
	}
	@Test
	public void test_3_2() {
		final boolean result = parse(":Person-{id} :firstName {fname}^^xsd:string .");
		assertTrue(result);
	}

	@Test
	public void test_3_concat() {
		final boolean result = parse(":Person-{id} :firstName \"hello {fname}\"^^xsd:string .");
		assertTrue(result);
	}

	@Test
	public void test_3_concat_number() {
		final boolean result = parse(":Person-{id} :firstName \"hello {fname}\"^^xsd:double .");
		assertTrue(result);
	}
	@Test
	public void test_3_3() {
		final boolean result = parse(":Person-{id} :firstName {fname}@en-US .");
		assertTrue(result);
	}

	@Test
	public void test_4_1_1() {
		final boolean result = parse(":Person-{id} :firstName \"John\"^^xsd:string .");
		assertTrue(result);
	}

	@Test
	public void test_4_1_2() {
		final boolean result = parse(":Person-{id} <http://example.org/testcase#firstName> \"John\"^^xsd:string .");
		assertTrue(result);
	}

	@Test
	public void test_4_2_1() {
		final boolean result = parse(":Person-{id} :firstName \"John\"^^rdfs:Literal .");
		assertTrue(result);
	}

	@Test
	public void test_4_2_2() {
		final boolean result = parse(":Person-{id} :firstName \"John\"@en-US .");
		assertTrue(result);
	}

	@Test
	public void test_5_1_1() {
		final boolean result = parse(":Person-{id} a :Person ; :firstName {fname} .");
		assertTrue(result);
	}

	@Test
	public void test_5_1_2() {
		final boolean result = parse(":Person-{id} a :Person ; :firstName {fname} ; :age {age} .");
		assertTrue(result);
	}

	@Test
	public void test_5_1_3() {
		final boolean result = parse(":Person-{id} a :Person ; :hasFather :Person-{id} ; :firstName {fname} ; :age {age} .");
		assertTrue(result);
	}

	@Test
	public void test_5_2_1() {
		final boolean result = parse(":Person-{id} a :Person ; :firstName {fname}^^xsd:string .");
		assertTrue(result);
	}

	@Test
	public void test_5_2_2() {
		final boolean result = parse(":Person-{id} a :Person ; :firstName {fname}^^xsd:string ; :age {age}^^xsd:integer .");
		assertTrue(result);
	}

	@Test
	public void test_5_2_3() {
		final boolean result = parse(":Person-{id} a :Person ; :hasFather :Person-{id} ; :firstName {fname}^^xsd:string ; :age {age}^^xsd:integer .");
		assertTrue(result);
	}

	@Test
	public void test_5_2_4() {
		final boolean result = parse(":Person-{id} a :Person ; :hasFather :Person-{id} ; :firstName {fname}^^xsd:string ; :age {age}^^xsd:integer ; :description {text}@en-US .");
		assertTrue(result);
	}

	@Test
	public void test_5_2_5() {
		final boolean result = parse(":Person-{id} a <http://example.org/testcase#Person> ; <http://example.org/testcase:hasFather> :Person-{id} ; <http://example.org/testcase#firstName> {fname}^^xsd:string ; <http://example.org/testcase#age> {age}^^xsd:integer ; <http://example.org/testcase#description> {text}@en-US .");
		assertTrue(result);
	}

	@Test
	public void test_6_1() {
		final boolean result = parse(":Person-{id} a :Person ; :firstName {fname}^^xsd:String .");
		assertFalse(result);
	}

	@Test
	public void test_6_1_literal() {
		final boolean result = parse(":Person-{id} a :Person ; :firstName \"Sarah\" .");
		assertTrue(result);
	}

	@Test
	public void test_6_2() {
		final boolean result = parse(":Person-{id} a :Person ; :firstName {fname}^^ex:randomDatatype .");
		assertFalse(result);
	}

	@Test
	public void test_7_1() {
		final boolean result = parse(":Person-{id} a :Person .");
		assertTrue(result);
	}

	@Test
	public void test_7_2() {
		final boolean result = parse(":Person-{id} :hasFather :Person-{id} .");
		assertTrue(result);
	}

	@Test
	public void test_8_1() {
		final boolean result = parse(":Person-{id} rdf:type :Person .");
		assertTrue(result);
	}

	@Test
	public void test_8_2() {
		final boolean result = parse("ex:Person-{id} rdf:type ex:Person .");
		assertTrue(result);
	}

	@Test
	public void test_8_3() {
		final boolean result = parse("ex:Person-{id} ex:hasFather ex:Person-123 .");
		assertTrue(result);
	}

	@Test
	public void test_8_4() {
		final boolean result = parse("ex:Person/{id}/ ex:hasFather ex:Person/123/ .");
		assertTrue(result);
	}

	//multiple triples with different subjects
	@Test
	public void test_9_1(){
		final boolean result = compareCQIE(":S_{id} a :Student ; :fname {first_name} ; :hasCourse :C_{course_id}  .\n" +
				":C_{course_id} a :Course ; :hasProfessor :P_{id} . \n" +
				":P_{id} a :Professor ; :teaches :C_{course_id} .\n" +
				"{first_name} a :Name . ", 8);
				assertTrue(result);

	}

	@Test
	public void test_9_2(){
		final boolean result = compareCQIE("{idEmigrante} a  :E21_Person ; :P131_is_identified_by {nome} ; :P11i_participated_in {numCM} .\n" +
				"{nome} a :E82_Actor_Appellation ; :P3_has_note {nome}^^xsd:string .\n" +
				"{numCM} a :E9_Move .", 6);
		assertTrue(result);

	}

	// The following tests are checking Bnode syntax
	@Test
	public void test_9_3() {
		final boolean result = parse("[] :hasFather :Person-{id} .");
		assertTrue(result);
	}


    @Test
    public void test_9_300() {
        final boolean result = parse("_:{id} :hasFather :Person-{id} .");
        assertTrue(result);
    }
    @Test
	public void test_9_4() {
		final boolean result = parse("[] :hasFather [] .");
		assertTrue(result);
	}

	@Test
	public void test_9_4_1() {
		final boolean result = parse("_:id a :Person .");
		assertTrue(result);
	}

	@Test
	public void test_9_5() {
		final boolean result = parse("[] :hasFather _:k .");
		assertTrue(result);
	}

	//Should this be recognised because "," is next to :Person-{id}
	@Test
	public void test_9_6() {
	//	final boolean result = parse("[] :hasFather [ :hasSibling Person-{id}, Person-{id1}  ] .");
        final boolean result = parse(":{id} :hasSibling :Person-{id},  :Person-{id1}.");
        assertTrue(result);
	}

	//This works in current parser implementation, but actually shouldnt work
	@Test
	public void test_9_6_1() {
		final boolean result = parse("[] :hasFather [ :hasFather :Person-{id} ; :hasMother :Person  ] .");
		assertTrue(result);
	}

	//This works in the current parser implementation, but actually shouldn't work
	//what is a Person here?
	//even with :Person doesn't work correctly
	@Test
	public void test_9_6_false() {
		final boolean result = parse("[] :hasFather [ :hasFather :Person-{id}, :hasMother :Person  ] .");
		assertTrue(result);
	}

	//A subject which is also a bnode is implicitly given by nesting in square brackets
	@Test
	public void test_9_7() {
		final boolean result = parse("[ :hasFather [ :hasFather :Person-{id}  ] .");
		assertTrue(result);
	}

	@Test
	public void test_9_8() {
		final boolean result = parse("[ :name \"Alice\" ] .");
		assertTrue(result);
	}

    @Test
    public void test_9_8_1() {
        final boolean result = parse("[ :name \"Alice\" ] :knows [  :name \"Bob\" ] .");
        assertTrue(result);
    }

    @Test
    public void test_9_9() {
        final boolean result = parse("[ :name \"Alice\" ]  :knows [  :name \"Bob\" ; :knows  [ :name \"Eve\" ] ;  :mbox \"mail\" ] .");
        assertTrue(result);
    }


		//Test for value constant
		@Test
	public void test10() {
		final boolean result = parse(":Person-{id} a :Person ; :age 25 ; :hasDegree true ; :averageGrade 28.3 .");
		assertTrue(result);
	}

	//Set of tests for labeled blank node templates
	@Test
	public void test10_1() {
		final boolean result = parse("_:{id}_{age}_{name} a :Person .");
		assertTrue(result);
	}





	private boolean compareCQIE(String input, int countBody) {
		TurtleOBDASyntaxParser parser = new TurtleOBDASyntaxParser();
		parser.setPrefixManager(getPrefixManager());
		List<Function> mapping;
		try {
			mapping = parser.parse(input);
		} catch (TargetQueryParserException e) {
			log.debug(e.getMessage());
			return false;
		} catch (Exception e) {
			log.debug(e.getMessage());
			return false;
		}
		return mapping.size()==countBody;
	}
	private boolean parse(String input) {
		TurtleOBDASyntaxParser parser = new TurtleOBDASyntaxParser();
		parser.setPrefixManager(getPrefixManager());
		List<Function> mapping;
		try {
			mapping = parser.parse(input);
			log.debug("mapping " + mapping);
		} catch (TargetQueryParserException e) {
			log.debug(e.getMessage());
			return false;
		} catch (Exception e) {
			log.debug(e.getMessage());
			return false;
		}
		return true;
	}
	
	private PrefixManager getPrefixManager() {
		PrefixManager pm = new SimplePrefixManager();
		pm.addPrefix(PrefixManager.DEFAULT_PREFIX, "http://obda.inf.unibz.it/testcase#");
		pm.addPrefix("ex:", "http://www.example.org/");
		return pm;
	}
}
