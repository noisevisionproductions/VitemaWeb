import * as React from "react"
import { cva, type VariantProps } from "class-variance-authority"
import { cn} from "../../../utils/cs";

const badgeVariants = cva(
    "inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium transition-colors focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2",
    {
        variants: {
            variant: {
                default:
                    "bg-primary text-primary-foreground hover:bg-primary/80",
                secondary:
                    "bg-secondary text-secondary-foreground hover:bg-secondary/80",
                destructive:
                    "bg-destructive text-destructive-foreground hover:bg-destructive/80",
                outline:
                    "border border-input hover:bg-accent hover:text-accent-foreground",
                success:
                    "bg-status-success bg-opacity-20 text-status-success border border-status-success border-opacity-20",
                warning:
                    "bg-status-warning bg-opacity-20 text-status-warning border border-status-warning border-opacity-20",
                error:
                    "bg-status-error bg-opacity-20 text-status-error border border-status-error border-opacity-20",
                info:
                    "bg-status-info bg-opacity-20 text-status-info border border-status-info border-opacity-20",
            },
            size: {
                default: "h-6 px-2.5 py-0.5 text-xs",
                sm: "h-5 px-2 py-0 text-xs",
                lg: "h-7 px-3 py-1 text-sm",
            },
        },
        defaultVariants: {
            variant: "default",
            size: "default",
        },
    }
)

export interface BadgeProps
    extends React.HTMLAttributes<HTMLDivElement>,
        VariantProps<typeof badgeVariants> {}

function Badge({ className, variant, size, ...props }: BadgeProps) {
    return (
        <div className={cn(badgeVariants({ variant, size }), className)} {...props} />
    )
}

export { Badge, badgeVariants }