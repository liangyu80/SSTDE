/*
 * Copyright 2011 by TalkingTrends (Amsterdam, The Netherlands)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://opensahara.com/licenses/apache-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.useekm.reposail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.openrdf.model.Literal;
import org.openrdf.model.Value;
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.Add;
import org.openrdf.query.algebra.AggregateOperatorBase;
import org.openrdf.query.algebra.And;
import org.openrdf.query.algebra.ArbitraryLengthPath;
import org.openrdf.query.algebra.Avg;
import org.openrdf.query.algebra.BNodeGenerator;
import org.openrdf.query.algebra.BindingSetAssignment;
import org.openrdf.query.algebra.Bound;
import org.openrdf.query.algebra.Clear;
import org.openrdf.query.algebra.Coalesce;
import org.openrdf.query.algebra.Compare;
import org.openrdf.query.algebra.CompareAll;
import org.openrdf.query.algebra.CompareAny;
import org.openrdf.query.algebra.Copy;
import org.openrdf.query.algebra.Count;
import org.openrdf.query.algebra.Create;
import org.openrdf.query.algebra.Datatype;
import org.openrdf.query.algebra.DeleteData;
import org.openrdf.query.algebra.Difference;
import org.openrdf.query.algebra.Distinct;
import org.openrdf.query.algebra.EmptySet;
import org.openrdf.query.algebra.Exists;
import org.openrdf.query.algebra.Extension;
import org.openrdf.query.algebra.ExtensionElem;
import org.openrdf.query.algebra.Filter;
import org.openrdf.query.algebra.FunctionCall;
import org.openrdf.query.algebra.Group;
import org.openrdf.query.algebra.GroupConcat;
import org.openrdf.query.algebra.GroupElem;
import org.openrdf.query.algebra.IRIFunction;
import org.openrdf.query.algebra.If;
import org.openrdf.query.algebra.In;
import org.openrdf.query.algebra.InsertData;
import org.openrdf.query.algebra.Intersection;
import org.openrdf.query.algebra.IsBNode;
import org.openrdf.query.algebra.IsLiteral;
import org.openrdf.query.algebra.IsNumeric;
import org.openrdf.query.algebra.IsResource;
import org.openrdf.query.algebra.IsURI;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.Label;
import org.openrdf.query.algebra.Lang;
import org.openrdf.query.algebra.LangMatches;
import org.openrdf.query.algebra.LeftJoin;
import org.openrdf.query.algebra.Like;
import org.openrdf.query.algebra.Load;
import org.openrdf.query.algebra.LocalName;
import org.openrdf.query.algebra.MathExpr;
import org.openrdf.query.algebra.Max;
import org.openrdf.query.algebra.Min;
import org.openrdf.query.algebra.Modify;
import org.openrdf.query.algebra.Move;
import org.openrdf.query.algebra.MultiProjection;
import org.openrdf.query.algebra.Namespace;
import org.openrdf.query.algebra.Not;
import org.openrdf.query.algebra.Or;
import org.openrdf.query.algebra.Order;
import org.openrdf.query.algebra.OrderElem;
import org.openrdf.query.algebra.Projection;
import org.openrdf.query.algebra.ProjectionElem;
import org.openrdf.query.algebra.ProjectionElemList;
import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.QueryModelVisitor;
import org.openrdf.query.algebra.QueryRoot;
import org.openrdf.query.algebra.Reduced;
import org.openrdf.query.algebra.Regex;
import org.openrdf.query.algebra.SameTerm;
import org.openrdf.query.algebra.Sample;
import org.openrdf.query.algebra.Service;
import org.openrdf.query.algebra.SingletonSet;
import org.openrdf.query.algebra.Slice;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.StatementPattern.Scope;
import org.openrdf.query.algebra.Str;
import org.openrdf.query.algebra.Sum;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.UnaryTupleOperator;
import org.openrdf.query.algebra.Union;
import org.openrdf.query.algebra.UpdateExpr;
import org.openrdf.query.algebra.ValueConstant;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.ZeroLengthPath;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.rio.ntriples.NTriplesUtil;

/**
 * Converts {@link TupleExpr} (parsed queries) back to SPARQL queries.
 * 
 * @see #translate(TupleExpr)
 */
public class TupleExprToSparqlConverter implements QueryModelVisitor<QueryEvaluationException> {
    private StringBuffer buffer;
    private BindingInfo bindingInfo;
    private Collection<String> projected;
    private SelectModifier selectModifier;
    private StringBuffer orderBy;
    private StringBuffer groupBy;
    private String prefix;
    private boolean isConstruct;

    private enum SelectModifier {
        distinct, reduced
    }

    /**
     * @param expr The {@link TupleExpr} that needs to be converted to a SPARQL query.
     * @return A string with the SPARQL query that represents expr.
     * 
     * @throws QueryEvaluationException when a conversion is not possible, for example because the {@link TupleExpr} contains constructs that are invalid in SPARQL.
     */
    public String translate(TupleExpr expr) throws QueryEvaluationException {
        buffer = new StringBuffer();
        projected = new ArrayList<String>(); //probably a bit faster than HashSet for small number of items...
        bindingInfo = new BindingInfo();
        expr.visit(new VarUsageCounter(bindingInfo));
        prefix = computeAnonymousPrefix(bindingInfo.varCounts);
        expr.visit(this);
        return buffer.toString();
    }

    @Override public void meet(And node) throws QueryEvaluationException {
        buffer.append('(');
        node.getLeftArg().visit(this);
        buffer.append(" && ");
        node.getRightArg().visit(this);
        buffer.append(')');
    }

    @Override public void meet(ArbitraryLengthPath node) throws QueryEvaluationException {
        throw new QueryEvaluationException("SPARQL 1.1 path expressions not yet fully supported");//TODO
    }

    @Override public void meet(Avg node) throws QueryEvaluationException {
        meetAggregate(node, "AVG");
    }

    @Override public void meet(BNodeGenerator node) throws QueryEvaluationException {
        buffer.append("[]");
    }

    @Override public void meet(Bound node) throws QueryEvaluationException {
        buffer.append("bound(");
        node.getArg().visit(this);
        buffer.append(')');
    }

    @Override public void meet(Coalesce node) throws QueryEvaluationException {
        buffer.append("COALESCE(");
        boolean komma = false;
        for (ValueExpr arg: node.getArguments()) {
            if (komma)
                buffer.append(", ");
            arg.visit(this);
            komma = true;
        }
        buffer.append(')');
    }

    @Override public void meet(Compare node) throws QueryEvaluationException {
        buffer.append('(');
        node.getLeftArg().visit(this);
        switch (node.getOperator()) {
        case EQ:
            buffer.append(" = ");
            break;
        case GE:
            buffer.append(" >= ");
            break;
        case GT:
            buffer.append(" > ");
            break;
        case LE:
            buffer.append(" <= ");
            break;
        case LT:
            buffer.append(" < ");
            break;
        default:
            Validate.isTrue(Compare.CompareOp.NE.equals(node.getOperator()));
            buffer.append(" != ");
            break;
        }
        node.getRightArg().visit(this);
        buffer.append(')');
    }

    @Override public void meet(CompareAll node) throws QueryEvaluationException {
        throw new QueryEvaluationException("CompareAll is not part of SPARQL");
    }

    @Override public void meet(CompareAny node) throws QueryEvaluationException {
        throw new QueryEvaluationException("CompareAny is not part of SPARQL");
    }

    @Override public void meet(Count node) throws QueryEvaluationException {
        meetAggregate(node, "COUNT");

    }

    @Override public void meet(Datatype node) throws QueryEvaluationException {
        buffer.append("datatype(");
        node.getArg().visit(this);
        buffer.append(')');
    }

    @Override public void meet(Difference node) throws QueryEvaluationException {
        buffer.append('{');
        node.getLeftArg().visit(this);
        buffer.append("} MINUS {");
        node.getRightArg().visit(this);
        buffer.append("}");
    }

    @Override public void meet(Distinct node) throws QueryEvaluationException {
        selectModifier = SelectModifier.distinct;
        node.getArg().visit(this);
    }

    @Override public void meet(EmptySet node) throws QueryEvaluationException {
        buffer.append("CONSTRUCT {} WHERE {}");//TODO: how to handle other occurences? {} does not have EmptySet semantics...
        QueryModelNode p = node.getParentNode();
        while (p != null) {
            Validate.isTrue(!(p instanceof Projection));
            p = p.getParentNode();
        }
    }

    @Override public void meet(Exists node) throws QueryEvaluationException {
        buffer.append("EXISTS {");
        node.getSubQuery().visit(this);
        buffer.append("}");
    }

    @Override public void meet(Extension node) throws QueryEvaluationException {
        node.getArg().visit(this);
        for (ExtensionElem elem: node.getElements()) {
            String name = elem.getName();
            if (name != null && !name.startsWith("-")) {//Handled by ConstantFinder tree walker
                buffer.append(' ');
                elem.visit(this);
            }
        }
    }

    @Override public void meet(ExtensionElem node) throws QueryEvaluationException {
        buffer.append("BIND(");
        node.getExpr().visit(this);
        buffer.append(" AS ?").append(node.getName()).append(')');
    }

    @Override public void meet(Filter node) throws QueryEvaluationException {
        boolean group = isHaving(node);
        if (group) {
            node.visit(new BindingsFinder(bindingInfo));
            bindingInfo.visitNext.visit(this);
        } else {
            buffer.append('{');
            node.getArg().visit(this);
            buffer.append("} ");
        }
        if (group) {
            StringBuffer oldBuffer = buffer;
            buffer = groupBy;
            buffer.append(" HAVING ");
            node.getCondition().visit(this);
            buffer = oldBuffer;
        } else if (node.getCondition() instanceof Exists) {
            buffer.append("FILTER EXISTS {");
            ((Exists)node.getCondition()).getSubQuery().visit(this);
            buffer.append('}');
        } else if (node.getCondition() instanceof Not && ((Not)node.getCondition()).getArg() instanceof Exists) {
            buffer.append("FILTER NOT EXISTS {");
            ((Exists)((Not)node.getCondition()).getArg()).getSubQuery().visit(this);
            buffer.append('}');
        } else {
            buffer.append("FILTER (");
            node.getCondition().visit(this);
            buffer.append(')');
        }
    }

    private boolean isHaving(Filter node) {
        TupleExpr expr = node.getArg();
        do {
            if (expr instanceof Group)
                return true;
            else if (expr instanceof UnaryTupleOperator) {
                if (expr instanceof MultiProjection || expr instanceof Projection)
                    expr = null;
                else
                    expr = ((UnaryTupleOperator)expr).getArg();
            } else
                expr = null;
        } while (expr != null);
        return false;
    }

    @Override public void meet(FunctionCall node) throws QueryEvaluationException {
        buffer.append('<').append(node.getURI()).append(">(");
        boolean first = true;
        for (ValueExpr expr: node.getArgs()) {
            if (!first)
                buffer.append(", ");
            expr.visit(this);
            first = false;
        }
        buffer.append(')');
    }

    @Override public void meet(Group node) throws QueryEvaluationException {
        node.getArg().visit(this);
        groupBy = new StringBuffer(" GROUP BY");
        for (String name: node.getGroupBindingNames())
            groupBy.append(" ?").append(name);
    }

    @Override public void meet(GroupConcat node) throws QueryEvaluationException {
        meetAggregate(node, "GROUP_CONCAT");
    }

    @Override public void meet(GroupElem node) throws QueryEvaluationException {
        node.visitChildren(this);
    }

    @Override public void meet(If node) throws QueryEvaluationException {
        buffer.append("IF(");
        node.getCondition().visit(this);
        buffer.append(", ");
        node.getResult().visit(this);
        buffer.append(", ");
        node.getAlternative().visit(this);
        buffer.append(")");
    }

    @Override public void meet(In node) throws QueryEvaluationException {
        throw new QueryEvaluationException("In is not part of SPARQL");
    }

    @Override public void meet(Intersection node) throws QueryEvaluationException {
        throw new QueryEvaluationException("Intersection is not part of SPARQL");
    }

    @Override public void meet(IRIFunction node) throws QueryEvaluationException {
        buffer.append("IRI(");
        node.getArg().visit(this);
        buffer.append(")");
    }

    @Override public void meet(IsNumeric node) throws QueryEvaluationException {
        buffer.append("isNUMERIC(");
        node.getArg().visit(this);
        buffer.append(")");
    }

    @Override public void meet(IsBNode node) throws QueryEvaluationException {
        buffer.append("isBlank(");
        node.getArg().visit(this);
        buffer.append(')');
    }

    @Override public void meet(IsLiteral node) throws QueryEvaluationException {
        buffer.append("isLiteral(");
        node.getArg().visit(this);
        buffer.append(')');
    }

    @Override public void meet(IsResource node) throws QueryEvaluationException {
        throw new QueryEvaluationException("IsResource is not part of SPARQL");
    }

    @Override public void meet(IsURI node) throws QueryEvaluationException {
        buffer.append("isURI(");
        node.getArg().visit(this);
        buffer.append(')');
    }

    @Override public void meet(Join node) throws QueryEvaluationException {
        buffer.append('{');
        node.getLeftArg().visit(this);
        buffer.append("}.{");
        node.getRightArg().visit(this);
        buffer.append('}');
    }

    @Override public void meet(Label node) throws QueryEvaluationException {
        throw new QueryEvaluationException("Label is not part of SPARQL");
    }

    @Override public void meet(Lang node) throws QueryEvaluationException {
        buffer.append("lang(");
        node.getArg().visit(this);
        buffer.append(')');
    }

    @Override public void meet(LangMatches node) throws QueryEvaluationException {
        buffer.append("langMatches(");
        node.getLeftArg().visit(this);
        buffer.append(", ");
        node.getRightArg().visit(this);
        buffer.append(')');
    }

    @Override public void meet(LeftJoin node) throws QueryEvaluationException {
        buffer.append('{');
        node.getLeftArg().visit(this);
        buffer.append("} OPTIONAL {");
        node.getRightArg().visit(this);
        buffer.append('}');
    }

    @Override public void meet(Like node) throws QueryEvaluationException {
        throw new QueryEvaluationException("Like is not part of SPARQL");
    }

    @Override public void meet(LocalName node) throws QueryEvaluationException {
        throw new QueryEvaluationException("LocalName is not part of SPARQL");
    }

    @Override public void meet(MathExpr node) throws QueryEvaluationException {
        buffer.append('(');
        node.getLeftArg().visit(this);
        buffer.append(" ");
        buffer.append(node.getOperator().getSymbol());
        buffer.append(" ");
        node.getRightArg().visit(this);
        buffer.append(')');
    }

    @Override public void meet(Max node) throws QueryEvaluationException {
        meetAggregate(node, "MAX");
    }

    @Override public void meet(Min node) throws QueryEvaluationException {
        meetAggregate(node, "MIN");
    }

    @Override public void meet(MultiProjection node) throws QueryEvaluationException {
        buffer.append("CONSTRUCT {");
        isConstruct = true;
        boolean needsDot = false;
        BindingsFinder bFinder = new BindingsFinder(bindingInfo);
        node.visit(bFinder);
        for (ProjectionElemList projection: node.getProjections()) {
            if (needsDot)
                buffer.append(" . ");
            boolean needsSpace = false;
            for (ProjectionElem projectionElem: projection.getElements()) {
                if (needsSpace)
                    buffer.append(' ');
                projectionElem.visit(this);
                needsSpace = true;
            }
            needsDot = true;
        }
        isConstruct = false;
        buffer.append("} WHERE {");
        node.getArg().visit(this);
        buffer.append('}');
        appendQueryModifiers();
    }

    @Override public void meet(Namespace node) throws QueryEvaluationException {
        throw new QueryEvaluationException("Namespace is not part of SPARQL");
    }

    @Override public void meet(Not node) throws QueryEvaluationException {
        if (node.getArg() instanceof Exists) {
            buffer.append("NOT ");
            node.getArg().visit(this);
        } else {
            buffer.append("!(");
            node.getArg().visit(this);
            buffer.append(')');
        }
    }

    @Override public void meet(Or node) throws QueryEvaluationException {
        buffer.append('(');
        node.getLeftArg().visit(this);
        buffer.append(" || ");
        node.getRightArg().visit(this);
        buffer.append(')');
    }

    @Override public void meet(Order order) throws QueryEvaluationException {
        order.getArg().visit(this);
        orderBy = new StringBuffer(" ORDER BY");
        StringBuffer oldBuffer = buffer;
        buffer = orderBy;
        for (OrderElem oe: order.getElements()) {
            buffer.append(' ');
            oe.visit(this);
        }
        buffer = oldBuffer;
    }

    @Override public void meet(OrderElem node) throws QueryEvaluationException {
        if (node.isAscending()) {
            buffer.append("ASC(");
            node.getExpr().visit(this);
            buffer.append(')');
        } else {
            buffer.append("DESC(");
            node.getExpr().visit(this);
            buffer.append(')');
        }
    }

    @Override public void meet(Projection node) throws QueryEvaluationException {
        isConstruct = node.getProjectionElemList().getElements().size() == 3 &&
            "subject".equals(node.getProjectionElemList().getElements().get(0).getTargetName()) &&
            "predicate".equals(node.getProjectionElemList().getElements().get(1).getTargetName()) &&
            "object".equals(node.getProjectionElemList().getElements().get(2).getTargetName());
        node.visit(new BindingsFinder(bindingInfo));
        TupleExpr visitNext = bindingInfo.visitNext;
        if (!isConstruct)
            buildSelect(node);
        else
            buildConstruct(node);
        isConstruct = false;
        buffer.append(" WHERE {");
        visitNext.visit(this);
        buffer.append('}');
        appendQueryModifiers();
    }

    private void buildSelect(Projection node) throws QueryEvaluationException {
        buffer.append("SELECT ");
        if (selectModifier == SelectModifier.distinct)
            buffer.append("DISTINCT ");
        else if (selectModifier == SelectModifier.reduced)
            buffer.append("REDUCED ");
        if (node.getProjectionElemList().getElements().isEmpty())
            buffer.append("*"); // we can't have an empty projection, but this is probably a query[part] without binding results...
        else
            meet(node.getProjectionElemList());
    }

    private void buildConstruct(Projection node) throws QueryEvaluationException {
        buffer.append("CONSTRUCT {");
        node.getProjectionElemList().getElements().get(0).visit(this);
        buffer.append(' ');
        node.getProjectionElemList().getElements().get(1).visit(this);
        buffer.append(' ');
        node.getProjectionElemList().getElements().get(2).visit(this);
        buffer.append("}");
    }

    @Override public void meet(ProjectionElem node) throws QueryEvaluationException {
        projected.add(node.getSourceName());
        ValueExpr value = bindingInfo.bindings.get(node.getSourceName());
        if (value == null)
            buffer.append('?').append(createName(node.getSourceName()));
        else if (value instanceof BNodeGenerator)
            value.visit(this);
        else if (!isConstruct) {
            buffer.append('(');
            value.visit(this);
            buffer.append(" AS ");
            buffer.append('?').append(createName(node.getSourceName()));
            buffer.append(')');
        } else
            value.visit(this);
    }

    @Override public void meet(ProjectionElemList node) throws QueryEvaluationException {
        boolean needSpace = false;
        for (ProjectionElem elem: node.getElements()) {
            if (needSpace)
                buffer.append(' ');
            elem.visit(this);
            needSpace = true;
        }
    }

    @Override public void meet(QueryRoot node) throws QueryEvaluationException {
        node.visitChildren(this);
    }

    @Override public void meet(Reduced node) throws QueryEvaluationException {
        selectModifier = SelectModifier.reduced;
        node.getArg().visit(this);
    }

    @Override public void meet(Regex node) throws QueryEvaluationException {
        buffer.append("regex(");
        node.getArg().visit(this);
        buffer.append(", ");
        node.getPatternArg().visit(this);
        if (node.getFlagsArg() != null) {
            buffer.append(", ");
            node.getFlagsArg().visit(this);
        }
        buffer.append(')');
    }

    @Override public void meet(SameTerm node) throws QueryEvaluationException {
        buffer.append("sameTerm(");
        node.getLeftArg().visit(this);
        buffer.append(", ");
        node.getRightArg().visit(this);
        buffer.append(')');
    }

    @Override public void meet(Sample node) throws QueryEvaluationException {
        meetAggregate(node, "SAMPLE");
    }

    @Override public void meet(Slice node) throws QueryEvaluationException {
        boolean ask = isAskQuery(node);
        if (ask)
            buffer.append("ASK {");
        node.getArg().visit(this);
        if (ask)
            buffer.append('}');
        else {
            long offset = node.getOffset();
            long limit = node.getLimit();
            if (offset > 0)
                buffer.append(" OFFSET ").append(String.valueOf(offset));
            if (limit > 0)
                buffer.append(" LIMIT ").append(String.valueOf(limit));
        }
    }

    private boolean isAskQuery(Slice node) {
        long offset = node.getOffset();
        long limit = node.getLimit();
        boolean ask = offset <= 0 && limit == 1; // Sesame parses ASK queries as LIMIT 1 queries without a projection
        TupleExpr expr = node.getArg();
        while (ask && (expr instanceof UnaryTupleOperator)) {
            if (expr instanceof Projection || expr instanceof MultiProjection)
                ask = false;
            expr = ((UnaryTupleOperator)expr).getArg();
        }
        return ask;
    }

    @Override public void meet(SingletonSet node) throws QueryEvaluationException {
        buffer.append("{}");
    }

    @Override public void meet(StatementPattern node) throws QueryEvaluationException {
        if (Scope.NAMED_CONTEXTS.equals(node.getScope())) {
            buffer.append("GRAPH ");
            node.getContextVar().visit(this);
            buffer.append(" {");
        }
        node.getSubjectVar().visit(this);
        buffer.append(' ');
        node.getPredicateVar().visit(this);
        buffer.append(' ');
        node.getObjectVar().visit(this);

        if (Scope.NAMED_CONTEXTS.equals(node.getScope()))
            buffer.append('}');
    }

    @Override public void meet(Str node) throws QueryEvaluationException {
        buffer.append("str(");
        node.getArg().visit(this);
        buffer.append(')');
    }

    @Override public void meet(Sum node) throws QueryEvaluationException {
        meetAggregate(node, "SUM");
    }

    @Override public void meet(Union node) throws QueryEvaluationException {
        buffer.append('{');
        node.getLeftArg().visit(this);
        buffer.append("} UNION {");
        node.getRightArg().visit(this);
        buffer.append('}');
    }

    public void meet(UpdateExpr node) throws QueryEvaluationException {
        throw new QueryEvaluationException("UPDATE SPARQL syntax not yet supported");
    }

    @Override public void meet(ValueConstant node) throws QueryEvaluationException {
        Value value = node.getValue();
        //shorter notation for some datatypes:
        if (value instanceof Literal && XMLSchema.INTEGER.equals(((Literal)value).getDatatype()))
            buffer.append(XMLDatatypeUtil.parseInt(((Literal)value).getLabel()));
        else if (value instanceof Literal && XMLSchema.BOOLEAN.equals(((Literal)value).getDatatype()))
            buffer.append(XMLDatatypeUtil.parseBoolean(((Literal)value).getLabel()));
        else
            buffer.append(NTriplesUtil.toNTriplesString(node.getValue()));
    }

    @Override public void meet(Var node) throws QueryEvaluationException {
        if (node.getValue() != null && !projected.contains(node.getName()))
            buffer.append(NTriplesUtil.toNTriplesString(node.getValue()));
        else if (bindingInfo.bindings.containsKey(node.getName()))
            bindingInfo.bindings.get(node.getName()).visit(this);
        else if (node.isAnonymous() && Integer.valueOf(1).equals(bindingInfo.varCounts.get(node.getName())))
            buffer.append("[]");
        else
            buffer.append('?').append(createName(node.getName()));
    }

    @Override public void meet(ZeroLengthPath node) throws QueryEvaluationException {
        throw new QueryEvaluationException("SPARQL 1.1 path expressions not yet fully supported");//TODO
    }

    @Override public void meetOther(QueryModelNode node) throws QueryEvaluationException {
        throw new QueryEvaluationException("Invalid SPARQL or unexpected call to meetOther during TupleExpr treewalker");
    }

    private String createName(String name) {
        if (name.startsWith("-"))
            return prefix + name.substring(1).replaceAll("-", "_");
        return name;
    }

    private static final class BindingInfo {
        Map<String, ValueExpr> bindings = new HashMap<String, ValueExpr>();
        Map<String, Integer> varCounts = new HashMap<String, Integer>();
        TupleExpr visitNext;
    }

    private final static class BindingsFinder extends QueryModelVisitorBase<QueryEvaluationException> {
        private BindingInfo bindings;

        private BindingsFinder(BindingInfo bindings) {
            this.bindings = bindings;
            bindings.bindings.clear();
        }

        @Override public void meet(Projection node) throws QueryEvaluationException {
            meetUnary(node);
        }

        @Override public void meet(MultiProjection node) throws QueryEvaluationException {
            meetUnary(node);
        }

        @Override public void meet(Filter node) throws QueryEvaluationException {
            meetUnary(node);
        }

        protected void meetUnary(UnaryTupleOperator node) throws QueryEvaluationException {
            bindings.visitNext = node.getArg();
            while (bindings.visitNext instanceof Extension) {
                //TODO: This probably can not continue if the Extension uses a name that is bound in a parent Extension in the getExpr for Projection/MultiProjection
                Extension extension = (Extension)bindings.visitNext;
                for (ExtensionElem extElm: extension.getElements())
                    extElm.visit(this);
                bindings.visitNext = extension.getArg();
            }
        }

        @Override public void meet(ExtensionElem node) throws QueryEvaluationException {
            bindings.bindings.put(node.getName(), node.getExpr());
        }
    }

    private final static class VarUsageCounter extends QueryModelVisitorBase<QueryEvaluationException> {
        private BindingInfo bindings;

        private VarUsageCounter(BindingInfo bindings) {
            this.bindings = bindings;
        }

        @Override public void meet(ExtensionElem node) throws QueryEvaluationException {
            count(node.getName());
        }

        @Override public void meet(ProjectionElem node) throws QueryEvaluationException {
            count(node.getSourceName());
        }

        @Override public void meet(Var var) throws QueryEvaluationException {
            count(var.getName());
        }

        private void count(String name) {
            Integer oldCount = bindings.varCounts.get(name);
            if (oldCount == null)
                bindings.varCounts.put(name, 1);
            else
                bindings.varCounts.put(name, oldCount + 1);
        }
    }

    private static String computeAnonymousPrefix(Map<String, Integer> varCounts) {
        String result = "a_";
        boolean success = false;
        while (!success) {
            success = true;
            for (Map.Entry<String, Integer> entry: varCounts.entrySet())
                if (entry.getKey().startsWith(result)) {
                    success = false;
                    result += '_';
                    break;
                }
        }
        return result;
    }

    private void meetAggregate(AggregateOperatorBase node, String name) throws QueryEvaluationException {
        buffer.append(name).append('(');
        if (node.isDistinct())
                buffer.append("DISTINCT ");
        node.getArg().visit(this);
        buffer.append(')');
    }

    private void appendQueryModifiers() {
        if (groupBy != null)
            buffer.append(groupBy);
        if (orderBy != null)
            buffer.append(orderBy);
        groupBy = null;
        orderBy = null;
    }

	@Override
	public void meet(BindingSetAssignment arg0) throws QueryEvaluationException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void meet(Service arg0) throws QueryEvaluationException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void meet(Add arg0) throws QueryEvaluationException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void meet(Clear arg0) throws QueryEvaluationException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void meet(Copy arg0) throws QueryEvaluationException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void meet(Create arg0) throws QueryEvaluationException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void meet(DeleteData arg0) throws QueryEvaluationException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void meet(InsertData arg0) throws QueryEvaluationException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void meet(Load arg0) throws QueryEvaluationException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void meet(Modify arg0) throws QueryEvaluationException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void meet(Move arg0) throws QueryEvaluationException {
		// TODO Auto-generated method stub
		
	}
}
