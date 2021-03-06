package org.jruby.ir.instructions;

import org.jruby.ir.IRVisitor;
import org.jruby.ir.Operation;
import org.jruby.ir.operands.Operand;
import org.jruby.ir.operands.Variable;
import org.jruby.ir.transformations.inlining.CloneInfo;
import org.jruby.ir.transformations.inlining.InlineCloneInfo;
import org.jruby.ir.transformations.inlining.SimpleCloneInfo;

public class ReturnInstr extends ReturnBase implements FixedArityInstr {
    public ReturnInstr(Operand returnValue) {
        super(Operation.RETURN, returnValue);
    }

    @Override
    public String toString() {
        return getOperation() + "(" + returnValue + ")";
    }

    @Override
    public Instr clone(CloneInfo info) {
        if (info instanceof SimpleCloneInfo) return new ReturnInstr(returnValue.cloneForInlining(info));

        InlineCloneInfo ii = (InlineCloneInfo) info;

        if (ii.isClosure()) return new CopyInstr(ii.getYieldResult(), returnValue.cloneForInlining(ii));

        Variable v = ii.getCallResultVariable();
        return v == null ? null : new CopyInstr(v, returnValue.cloneForInlining(ii));
    }

    @Override
    public void visit(IRVisitor visitor) {
        visitor.ReturnInstr(this);
    }
}
