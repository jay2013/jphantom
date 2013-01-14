package jphantom.tree;

import util.Command;
import java.util.*;
import jphantom.Types;
import org.objectweb.asm.Type;

public class Importer implements Command, Types
{
    private final ClassHierarchy target;
    private final ClassHierarchy source;

    public Importer(ClassHierarchy target, ClassHierarchy source) {
        this.target = target;
        this.source = source;
    }

    public Importer(ClassHierarchy target) {
        this(target, SystemClassHierarchy.getInstance());
    }

    protected ClassHierarchy getTarget() {
        return target;
    }

    protected ClassHierarchy getSource() {
        return source;
    }

    @Override
    public void execute()
    {
        for (Type i : target)
        {
            Set<Type> types = new HashSet<>(target.getInterfaces(i));
            Type sc = target.getSuperclass(i);
            
            if (sc != null)
                types.add(sc);

            for (Type j : types)
                if (!target.contains(j) && source.contains(j))
                    importFrom(j);
        }
    }

    public void execute(Type el)
    {
        if (!source.contains(el))
            throw new IllegalArgumentException();

        importFrom(el);
    }

    public void importFrom(Type root)
    {
        if (root == null || !source.contains(root))
            return;

        if (!target.contains(root))
        {
            Type sc = source.getSuperclass(root);
            Set<Type> ifaces = source.getInterfaces(root);

            // Recursively import supertypes

            for (Type i : ifaces)
                importFrom(i);

            importFrom(sc);

            // Import this type, after every supertype
            // has already been added

            if (source.isInterface(root))
                target.addInterface(root, ifaces.toArray(new Type[0]));
            else
                target.addClass(root, sc, ifaces.toArray(new Type[0]));
        }
    }
}
