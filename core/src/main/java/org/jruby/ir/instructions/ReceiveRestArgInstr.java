package org.jruby.ir.instructions;

import org.jruby.ir.IRVisitor;
import org.jruby.ir.Operation;
import org.jruby.ir.operands.Fixnum;
import org.jruby.ir.operands.Operand;
import org.jruby.ir.operands.Variable;
import org.jruby.ir.runtime.IRRuntimeHelpers;
import org.jruby.ir.transformations.inlining.CloneInfo;
import org.jruby.ir.transformations.inlining.InlineCloneInfo;
import org.jruby.ir.transformations.inlining.SimpleCloneInfo;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

/*
 * Assign rest arg passed into method to a result variable
 */
public class ReceiveRestArgInstr extends ReceiveArgBase implements FixedArityInstr {
    /** Number of arguments already accounted for */
    public final int required;

    public ReceiveRestArgInstr(Variable result, int required, int argIndex) {
        super(Operation.RECV_REST_ARG, result, argIndex);
        this.required = required;
    }

    @Override
    public String toString() {
        return (isDead() ? "[DEAD]" : "") + (hasUnusedResult() ? "[DEAD-RESULT]" : "") + getResult() + " = " + getOperation() + "(" + required + ", " + argIndex + ")";
    }

    @Override
    public Operand[] getOperands() {
        return new Operand[] { new Fixnum(required), new Fixnum(argIndex) };
    }

    @Override
    public Instr clone(CloneInfo info) {
        if (info instanceof SimpleCloneInfo) return new ReceiveRestArgInstr(info.getRenamedVariable(result), required, argIndex);

        InlineCloneInfo ii = (InlineCloneInfo) info;

        // FIXME: Check this
        if (ii.canMapArgsStatically()) return new CopyInstr(ii.getRenamedVariable(result), ii.getArg(argIndex, true));

        return new RestArgMultipleAsgnInstr(ii.getRenamedVariable(result), ii.getArgs(), argIndex, (required - argIndex), argIndex);
    }

    @Override
    public IRubyObject receiveArg(ThreadContext context, IRubyObject[] args,  boolean acceptsKeywordArguments) {
        return IRRuntimeHelpers.receiveRestArg(context, args, required, argIndex, acceptsKeywordArguments);
    }

    @Override
    public void visit(IRVisitor visitor) {
        visitor.ReceiveRestArgInstr(this);
    }
}
