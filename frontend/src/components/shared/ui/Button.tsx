import * as React from "react"
import {cn} from "../../../utils/cs";

export interface ButtonProps
    extends React.ButtonHTMLAttributes<HTMLButtonElement> {
    variant?: "default" | "destructive" | "outline" | "ghost" | "link" | "primary" | "secondary"
    size?: "default" | "sm" | "lg" | "icon"
}

const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
    ({className, variant = "default", size = "default", ...props}, ref) => {
        return (
            <button
                className={cn(
                    "inline-flex items-center justify-center whitespace-nowrap rounded-md text-sm font-medium transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-gray-950 disabled:pointer-events-none disabled:opacity-50",
                    {
                        "bg-gray-900 text-gray-50 shadow hover:bg-gray-900/90 dark:bg-gray-50 dark:text-gray-900 dark:hover:bg-gray-200":
                            variant === "default",

                        "bg-red-500 text-gray-50 shadow-sm hover:bg-red-500/90 dark:bg-red-600 dark:text-white":
                            variant === "destructive",

                        "border border-gray-200 bg-white shadow-sm hover:bg-gray-100 hover:text-gray-900 dark:border-gray-700 dark:bg-transparent dark:text-gray-100 dark:hover:bg-gray-800":
                            variant === "outline",

                        "hover:bg-gray-100 hover:text-gray-900 dark:text-gray-100 dark:hover:bg-gray-800":
                            variant === "ghost",

                        "text-gray-900 underline-offset-4 hover:underline dark:text-gray-100":
                            variant === "link",

                        "bg-primary text-white shadow hover:bg-primary-dark":
                            variant === "primary",
                        "bg-secondary text-white shadow hover:bg-secondary-dark":
                            variant === "secondary",

                        // Rozmiary
                        "h-9 px-4 py-2":
                            size === "default",
                        "h-8 rounded-md px-3 text-xs":
                            size === "sm",
                        "h-10 rounded-md px-8":
                            size === "lg",
                        "h-9 w-9 p-0":
                            size === "icon",
                    },
                    className
                )}
                ref={ref}
                {...props}
            />
        )
    }
)
Button.displayName = "Button"

export {Button}