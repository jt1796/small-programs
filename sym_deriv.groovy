eval = [
  'var': { x            -> { _ -> x } },
  'mul': { l, r, x      -> { _ -> compile(l, x)() * compile(r, x)() } },
  'sin': { e, x         -> { _ -> Math.sin(compile(e, x)()) } },
  'pow': { base, exp, x -> { _ -> Math.pow(compile(base, x)(), compile(exp, x)()) } },
  'log': { b, x         -> { _ -> Math.log(compile(b, x))() }},
  'add': { l, r, x      -> { _ -> compile(l, x)() + compile(r, x)() }},
  'con': { it, x        -> { _ -> it } }
]

// Double invocation ()(). Really needed? 
deriv_rules = [
  'var': { x            -> { _ -> ['con', 1] } },
  'mul': { l, r, x      -> { _ -> ['add', ['mul', derive(l)(), r], ['mul', l, derive(r)()]] } },
  'sin': { e, x         -> { _ -> ['mul', ['cos', e], derive(e)()] } },
  'add': { l, r, x      -> { _ -> ['add', derive(l)(), derive(r)()]} },
  'con': { it, x        -> { _ -> ['con', 0] } }
]

print_rules = [
  'var': { 'x' },
  'con': { it },
  'sin': { "Sin($it)" },
  'cos': { "Cos($it)" },
  'add': { l, r -> "$l + $r" },
  'mul': { l, r -> "$l * $r" }
]

def compile(expr, x='sentinel') {
    if ('sentinel' == x) {
        return { var -> compile(expr, var)() }
    }
    
    return eval[expr[0]](*expr.drop(1), x)
}

def derive(expr, x='sentinel') {
    return deriv_rules[expr[0]](*expr.drop(1), x)
}

// TODO:
//   1 * something
//   0 * something
//   0 + something
def prune_expr(expr) {
    if (expr[0] == 'mul') {
        if (expr[1] == ['con', 1]) {
            return expr[2]
        }
        if (expr[2] == ['con', 1]) {
            return expr[1]
        }
        // ...
    }
    return expr
}

// TODO: if depth is 1, no need for ()s
def print_expr(expr) {
    def args = expr.drop(1).collect { it -> it instanceof List ? "(${print_expr(it)})" : it }
    return print_rules[expr[0]](*   args)
}

// x * sin(x^2)
def expr = ['mul', 
               ['var'],
               ['sin',
                   ['pow', 
                       ['var'],
                       ['con', 2]
                   ]
               ]
           ]
       
compiled = compile(expr)
assert -1.5136049906158564 == compiled(2)

// x * sin(x)   ->  sinx + xcosx
expr = ['mul', ['sin', ['var']], ['var']]
println derive(expr)()
println print_expr(derive(expr)())
